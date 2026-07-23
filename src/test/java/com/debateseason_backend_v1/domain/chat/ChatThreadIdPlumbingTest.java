package com.debateseason_backend_v1.domain.chat;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.response.ChatMessageResponse;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;

/**
 * v1.3.5 Phase 1 — threadId 배선 검증.
 *
 * 요청(ChatMessageRequest) → 엔티티(ChatEntity) → 응답(ChatMessageResponse) 로 threadId 가
 * 유실 없이 흐르는지, 그리고 구 앱(threadId 미전송)은 NULL 로 안전하게 저장되는지 고정한다.
 * 컨테이너 라우팅/스레드 필터 쿼리는 이후 Phase 소관이므로 여기서 다루지 않는다.
 */
class ChatThreadIdPlumbingTest {

	private static final Long CONTAINER_ROOM_ID = 10L;
	private static final Long THREAD_ID = 55L;

	@Test
	@DisplayName("threadId 가 요청→엔티티→응답으로 유실 없이 전달된다")
	void threadIdRoundTrips() {
		// given
		ChatRoom container = ChatRoom.builder().id(CONTAINER_ROOM_ID).build();
		ChatMessageRequest request = ChatMessageRequest.builder()
			.roomId(CONTAINER_ROOM_ID)
			.threadId(THREAD_ID)
			.messageType(MessageType.CHAT)
			.content("원전 확대 찬성합니다")
			.sender("홍길동")
			.build();

		// when
		ChatEntity entity = ChatEntity.from(request, container, 1L);
		ChatMessageResponse response = ChatMessageResponse.from(entity, "RED");
		ChatMessageResponse fromRequest = ChatMessageResponse.from(request);

		// then
		assertThat(entity.getThreadId()).isEqualTo(THREAD_ID);
		assertThat(response.getThreadId()).isEqualTo(THREAD_ID);
		assertThat(response.getRoomId()).isEqualTo(CONTAINER_ROOM_ID);
		assertThat(fromRequest.getThreadId()).isEqualTo(THREAD_ID);
	}

	@Test
	@DisplayName("구 앱처럼 threadId 를 안 보내면 NULL 로 저장된다 (미분류)")
	void threadIdNullWhenAbsent() {
		// given
		ChatRoom room = ChatRoom.builder().id(CONTAINER_ROOM_ID).build();
		ChatMessageRequest legacyRequest = ChatMessageRequest.builder()
			.roomId(CONTAINER_ROOM_ID)
			.messageType(MessageType.CHAT)
			.content("구 앱 메시지")
			.sender("구앱유저")
			.build();

		// when
		ChatEntity entity = ChatEntity.from(legacyRequest, room, 1L);
		ChatMessageResponse response = ChatMessageResponse.from(entity, null);

		// then
		assertThat(entity.getThreadId()).isNull();
		assertThat(response.getThreadId()).isNull();
	}
}
