package com.debateseason_backend_v1.domain.chat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.enums.OpinionType;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;
import com.debateseason_backend_v1.domain.chat.application.service.ChatServiceV1;
import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chat.validation.ChatValidate;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileEntity;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileJpaRepository;
import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.entity.UserChatRoom;

/**
 * 채팅 찬반(opinionType)의 결정 권한이 서버에 있는지 고정한다.
 *
 * 토론방 정책은 "입장을 고르지 않으면 발언할 수 없다" 인데, 예전에는 클라이언트가 보낸
 * opinionType 을 그대로 저장해서 (1) 미투표 발언과 (2) 실제 투표와 다른 입장으로의 위장이
 * 둘 다 가능했다. sender 를 프로필에서 채우는 것과 같은 이유로 서버가 투표 기록에서 채운다.
 *
 * 투표는 스레드(=옛 채팅방) 단위로 user_chat_room 에 있고, 미투표는 행 자체가 없다.
 *
 * 발언 자격(게이팅)은 "행이 없다" 하나로 끝나지 않는다. 행이 있어도 값이 NEUTRAL 이거나
 * enum 밖 문자열이면 자격이 없고, 투표를 어느 방에서 찾는지도 방 종류마다 다르다
 * (스레드 / 컨테이너+스레드지정 / 레거시 / 컨테이너 전체). 그 네 경로를 모두 고정한다.
 */
class ChatOpinionServerAuthorityTest {

	private static final Long THREAD_ROOM_ID = 87L;
	private static final Long CONTAINER_ROOM_ID = 166L;
	// room_type 이 아직 NULL 인 이관 전 방. 운영 채팅방 상당수가 여기 해당한다.
	private static final Long LEGACY_ROOM_ID = 42L;
	private static final Long USER_ID = 3L;

	private final ChatRepository chatRepository = mock(ChatRepository.class);
	private final ChatRoomServiceV1 chatRoomService = mock(ChatRoomServiceV1.class);
	private final ChatValidate chatValidate = mock(ChatValidate.class);
	private final ProfileJpaRepository profileJpaRepository = mock(ProfileJpaRepository.class);
	private final UserChatRoomRepository userChatRoomRepository = mock(UserChatRoomRepository.class);

	private final ChatServiceV1 chatService = new ChatServiceV1(
		chatRepository, chatRoomService, chatValidate, null, null, null,
		profileJpaRepository, userChatRoomRepository);

	private final ChatRoom thread = ChatRoom.builder()
		.id(THREAD_ROOM_ID).roomType(ChatRoomType.THREAD).containerRoomId(CONTAINER_ROOM_ID).build();
	private final ChatRoom container = ChatRoom.builder()
		.id(CONTAINER_ROOM_ID).roomType(ChatRoomType.CONTAINER).build();
	// 레거시: roomType, containerRoomId 모두 미설정 → 방 자체가 투표 단위다.
	private final ChatRoom legacy = ChatRoom.builder()
		.id(LEGACY_ROOM_ID).build();

	private void givenProfile() {
		given(profileJpaRepository.findByUserId(USER_ID)).willReturn(
			Optional.of(ProfileEntity.builder().userId(USER_ID).nickname("주먹킹").profileImage("RED").build()));
		given(chatRepository.save(any(ChatEntity.class))).willAnswer(call -> call.getArgument(0));
	}

	private void givenVote(String opinion) {
		givenVoteOn(THREAD_ROOM_ID, opinion);
	}

	private void givenVoteOn(Long voteRoomId, String opinion) {
		UserChatRoom vote = opinion == null ? null : UserChatRoom.builder().opinion(opinion).build();
		given(userChatRoomRepository.findByUserIdAndChatRoomId(USER_ID, voteRoomId)).willReturn(vote);
	}

	private void givenThreadRouting() {
		given(chatRoomService.findChatRoomById(THREAD_ROOM_ID)).willReturn(thread);
		given(chatRoomService.findChatRoomById(CONTAINER_ROOM_ID)).willReturn(container);
	}

	private void assertRejectedAsUnvoted(Long roomId, Long threadId) {
		assertThatThrownBy(() -> chatService.processChatMessage(
			roomId, message(roomId, threadId, OpinionType.AGREE), authenticatedAs(USER_ID)))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException)e).getCodeInterface())
			.isEqualTo(ErrorCode.OPINION_REQUIRED_TO_CHAT);

		verify(chatRepository, never()).save(any());
	}

	private SimpMessageHeaderAccessor authenticatedAs(Long userId) {
		SimpMessageHeaderAccessor accessor = mock(SimpMessageHeaderAccessor.class);
		Principal principal = mock(Principal.class);
		given(principal.getName()).willReturn(String.valueOf(userId));
		given(accessor.getUser()).willReturn(principal);
		return accessor;
	}

	private ChatMessageRequest message(Long roomId, Long threadId, OpinionType claimed) {
		return ChatMessageRequest.builder()
			.roomId(roomId)
			.threadId(threadId)
			.messageType(MessageType.CHAT)
			.content("단순한 행정 실수로 넘길 문제는 아니라고 봅니다.")
			.opinionType(claimed)
			.timeStamp(LocalDateTime.now())
			.build();
	}

	private ChatEntity captureSaved() {
		ArgumentCaptor<ChatEntity> captor = ArgumentCaptor.forClass(ChatEntity.class);
		verify(chatRepository).save(captor.capture());
		return captor.getValue();
	}

	@Test
	@DisplayName("클라이언트가 NEUTRAL 을 보내도 실제 투표(AGREE)로 저장된다")
	void neutralFromClientIsReplacedByActualVote() {
		givenThreadRouting();
		givenProfile();
		givenVote("AGREE");

		chatService.processChatMessage(
			THREAD_ROOM_ID, message(THREAD_ROOM_ID, null, OpinionType.NEUTRAL), authenticatedAs(USER_ID));

		assertThat(captureSaved().getOpinionType()).isEqualTo(OpinionType.AGREE);
	}

	@Test
	@DisplayName("실제 투표와 다른 입장을 보내면 투표 기록으로 덮어써진다 (입장 위장 차단)")
	void claimedOpinionCannotOverrideActualVote() {
		givenThreadRouting();
		givenProfile();
		givenVote("DISAGREE");

		chatService.processChatMessage(
			THREAD_ROOM_ID, message(THREAD_ROOM_ID, null, OpinionType.AGREE), authenticatedAs(USER_ID));

		assertThat(captureSaved().getOpinionType()).isEqualTo(OpinionType.DISAGREE);
	}

	@Test
	@DisplayName("투표 기록이 없으면 발행이 거절된다 (에러 큐로 사유가 나간다)")
	void unvotedUserCannotChat() {
		givenThreadRouting();
		givenProfile();
		givenVote(null);

		assertRejectedAsUnvoted(THREAD_ROOM_ID, null);
	}

	@Test
	@DisplayName("스레드에 속하지 않는 '전체' 발언은 NEUTRAL 로 저장된다 (고를 입장이 없음)")
	void containerWideMessageStaysNeutral() {
		given(chatRoomService.findChatRoomById(CONTAINER_ROOM_ID)).willReturn(container);
		givenProfile();

		chatService.processChatMessage(
			CONTAINER_ROOM_ID, message(CONTAINER_ROOM_ID, null, OpinionType.AGREE), authenticatedAs(USER_ID));

		ChatEntity saved = captureSaved();
		assertThat(saved.getThreadId()).isNull();
		assertThat(saved.getOpinionType()).isEqualTo(OpinionType.NEUTRAL);
		verify(userChatRoomRepository, never()).findByUserIdAndChatRoomId(any(), any());
	}

	@Test
	@DisplayName("투표 값이 'NEUTRAL' 로 남아 있어도 발행이 거절된다 (행이 있다고 발언 자격은 아니다)")
	void neutralVoteRecordCannotChat() {
		// user_chat_room 에 행은 있지만 중립이다. 옛 코드가 클라이언트의 NEUTRAL 을 그대로
		// 저장하던 시절의 잔재로 실제 운영에도 있었다 (chat 525, 2026-08-22 삭제).
		givenThreadRouting();
		givenProfile();
		givenVote("NEUTRAL");

		assertRejectedAsUnvoted(THREAD_ROOM_ID, null);
	}

	@Test
	@DisplayName("enum 밖의 투표 값은 미투표로 처리된다 (500 이 아니라 거절이다)")
	void unknownVoteValueIsTreatedAsUnvoted() {
		// user_chat_room.opinion 은 varchar 라 enum 밖의 값이 들어갈 수 있다.
		// parseOpinion 의 try/catch 가 없어지면 IllegalArgumentException 이 그대로 올라가
		// 거절이 아니라 서버 오류가 된다. 그 경계를 고정한다.
		givenThreadRouting();
		givenProfile();

		for (String stored : new String[] {"yes", "agree", "", "AGREE "}) {
			givenVote(stored);
			assertRejectedAsUnvoted(THREAD_ROOM_ID, null);
		}
	}

	@Test
	@DisplayName("레거시 방(room_type 미설정)은 그 방 자체의 투표로 발언 자격을 판정한다")
	void legacyRoomUsesItsOwnVote() {
		// 이관 전 방은 컨테이너/스레드 구분이 없다. 라우팅이 바뀌어도 이 경로가 살아 있어야 한다.
		given(chatRoomService.findChatRoomById(LEGACY_ROOM_ID)).willReturn(legacy);
		givenProfile();
		givenVoteOn(LEGACY_ROOM_ID, "DISAGREE");

		chatService.processChatMessage(
			LEGACY_ROOM_ID, message(LEGACY_ROOM_ID, null, OpinionType.AGREE), authenticatedAs(USER_ID));

		ChatEntity saved = captureSaved();
		assertThat(saved.getOpinionType()).isEqualTo(OpinionType.DISAGREE);
		assertThat(saved.getThreadId()).isNull();
		verify(userChatRoomRepository).findByUserIdAndChatRoomId(USER_ID, LEGACY_ROOM_ID);
	}

	@Test
	@DisplayName("레거시 방도 미투표면 거절된다 (컨테이너 '전체' 발언처럼 NEUTRAL 로 새지 않는다)")
	void legacyRoomWithoutVoteIsRejected() {
		// roomType 이 CONTAINER 가 아니므로 voteRoomId 는 null 이 아니다.
		// 여기서 NEUTRAL 로 빠지면 이관 전 방 전체에서 게이팅이 뚫린다.
		given(chatRoomService.findChatRoomById(LEGACY_ROOM_ID)).willReturn(legacy);
		givenProfile();
		givenVoteOn(LEGACY_ROOM_ID, null);

		assertRejectedAsUnvoted(LEGACY_ROOM_ID, null);
	}

	@Test
	@DisplayName("컨테이너에 스레드를 지정해 보내면 그 스레드의 투표로 판정한다 (웹 스레드 탭)")
	void containerWithExplicitThreadUsesThreadVote() {
		// 컨테이너로 보내되 threadId 를 지정하는 경로. 컨테이너에는 투표가 달리지 않으므로
		// 지정된 스레드를 봐야 한다. 컨테이너를 보면 항상 미투표라 아무도 발언할 수 없게 된다.
		given(chatRoomService.findChatRoomById(CONTAINER_ROOM_ID)).willReturn(container);
		givenProfile();
		givenVoteOn(THREAD_ROOM_ID, "AGREE");

		chatService.processChatMessage(
			CONTAINER_ROOM_ID, message(CONTAINER_ROOM_ID, THREAD_ROOM_ID, OpinionType.DISAGREE),
			authenticatedAs(USER_ID));

		ChatEntity saved = captureSaved();
		assertThat(saved.getThreadId()).isEqualTo(THREAD_ROOM_ID);
		assertThat(saved.getOpinionType()).isEqualTo(OpinionType.AGREE);
		verify(userChatRoomRepository).findByUserIdAndChatRoomId(USER_ID, THREAD_ROOM_ID);
	}
}
