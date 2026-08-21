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
 */
class ChatOpinionServerAuthorityTest {

	private static final Long THREAD_ROOM_ID = 87L;
	private static final Long CONTAINER_ROOM_ID = 166L;
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

	private void givenProfile() {
		given(profileJpaRepository.findByUserId(USER_ID)).willReturn(
			Optional.of(ProfileEntity.builder().userId(USER_ID).nickname("주먹킹").profileImage("RED").build()));
		given(chatRepository.save(any(ChatEntity.class))).willAnswer(call -> call.getArgument(0));
	}

	private void givenVote(String opinion) {
		UserChatRoom vote = opinion == null ? null : UserChatRoom.builder().opinion(opinion).build();
		given(userChatRoomRepository.findByUserIdAndChatRoomId(USER_ID, THREAD_ROOM_ID)).willReturn(vote);
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
		given(chatRoomService.findChatRoomById(THREAD_ROOM_ID)).willReturn(thread);
		given(chatRoomService.findChatRoomById(CONTAINER_ROOM_ID)).willReturn(container);
		givenProfile();
		givenVote("AGREE");

		chatService.processChatMessage(
			THREAD_ROOM_ID, message(THREAD_ROOM_ID, null, OpinionType.NEUTRAL), authenticatedAs(USER_ID));

		assertThat(captureSaved().getOpinionType()).isEqualTo(OpinionType.AGREE);
	}

	@Test
	@DisplayName("실제 투표와 다른 입장을 보내면 투표 기록으로 덮어써진다 (입장 위장 차단)")
	void claimedOpinionCannotOverrideActualVote() {
		given(chatRoomService.findChatRoomById(THREAD_ROOM_ID)).willReturn(thread);
		given(chatRoomService.findChatRoomById(CONTAINER_ROOM_ID)).willReturn(container);
		givenProfile();
		givenVote("DISAGREE");

		chatService.processChatMessage(
			THREAD_ROOM_ID, message(THREAD_ROOM_ID, null, OpinionType.AGREE), authenticatedAs(USER_ID));

		assertThat(captureSaved().getOpinionType()).isEqualTo(OpinionType.DISAGREE);
	}

	@Test
	@DisplayName("투표 기록이 없으면 발행이 거절된다 (에러 큐로 사유가 나간다)")
	void unvotedUserCannotChat() {
		given(chatRoomService.findChatRoomById(THREAD_ROOM_ID)).willReturn(thread);
		given(chatRoomService.findChatRoomById(CONTAINER_ROOM_ID)).willReturn(container);
		givenProfile();
		givenVote(null);

		assertThatThrownBy(() -> chatService.processChatMessage(
			THREAD_ROOM_ID, message(THREAD_ROOM_ID, null, OpinionType.AGREE), authenticatedAs(USER_ID)))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException)e).getCodeInterface())
			.isEqualTo(ErrorCode.OPINION_REQUIRED_TO_CHAT);

		verify(chatRepository, never()).save(any());
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
}
