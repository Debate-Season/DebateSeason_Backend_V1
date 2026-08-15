package com.debateseason_backend_v1.domain.chat;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.response.ChatMessageResponse;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;

/**
 * ChatMessageResponse.userId 노출 검증.
 *
 * 클라이언트는 이 값을 JWT 의 sub 와 비교해 "내 메시지" 를 판별한다(웹 전체 탭의 좌우 정렬).
 * 닉네임(sender)은 발화 시점 스냅샷이라 개명·탈퇴 후 매칭이 깨지므로 판별 근거로 쓸 수 없다.
 *
 * 소켓 브로드캐스트와 REST 조회가 같은 판별 근거를 갖도록, 엔티티 기반 팩토리 전부가
 * userId 를 싣는지 고정한다. WebSocket 인증 게이트 이전 메시지는 user_id 가 없으므로
 * null 이 그대로 내려가야 한다(클라이언트는 판별 불가로 취급한다).
 */
class ChatMessageResponseUserIdTest {

	private static final Long CONTAINER_ROOM_ID = 10L;
	private static final Long THREAD_ID = 55L;
	private static final Long USER_ID = 123L;

	private ChatEntity chatBy(Long userId) {
		ChatRoom container = ChatRoom.builder().id(CONTAINER_ROOM_ID).build();
		ChatMessageRequest request = ChatMessageRequest.builder()
			.roomId(CONTAINER_ROOM_ID)
			.threadId(THREAD_ID)
			.messageType(MessageType.CHAT)
			.content("원전 확대 찬성합니다")
			.sender("홍길동")
			.build();
		return ChatEntity.from(request, container, userId);
	}

	@Test
	@DisplayName("소켓 브로드캐스트 응답에 작성자 userId 가 실린다")
	void userIdExposedOnBroadcast() {
		ChatEntity entity = chatBy(USER_ID);

		ChatMessageResponse response = ChatMessageResponse.from(entity, "RED");

		assertThat(response.getUserId()).isEqualTo(USER_ID);
	}

	@Test
	@DisplayName("발행 주소로 roomId 를 덮어써도 userId 는 유지된다")
	void userIdSurvivesAddressedRoomIdOverride() {
		ChatEntity entity = chatBy(USER_ID);
		Long addressedRoomId = THREAD_ID; // 구 앱·웹이 스레드 방 주소로 발행한 경우

		ChatMessageResponse response = ChatMessageResponse.from(entity, "RED", addressedRoomId);

		assertThat(response.getRoomId()).isEqualTo(addressedRoomId);
		assertThat(response.getUserId()).isEqualTo(USER_ID);
	}

	@Test
	@DisplayName("REST 목록 조회(배치 경로) 응답에도 userId 가 실린다")
	void userIdExposedOnOptimizedListing() {
		ChatEntity entity = chatBy(USER_ID);

		ChatMessageResponse response = ChatMessageResponse.fromOptimized(
			entity, USER_ID, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

		assertThat(response.getUserId()).isEqualTo(USER_ID);
	}

	@Test
	@DisplayName("인증 게이트 이전 메시지는 userId 가 null 로 내려간다")
	void userIdNullForLegacyMessage() {
		ChatEntity legacy = chatBy(null);

		ChatMessageResponse broadcast = ChatMessageResponse.from(legacy, null);
		ChatMessageResponse listing = ChatMessageResponse.fromOptimized(
			legacy, USER_ID, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

		assertThat(broadcast.getUserId()).isNull();
		assertThat(listing.getUserId()).isNull();
	}
}
