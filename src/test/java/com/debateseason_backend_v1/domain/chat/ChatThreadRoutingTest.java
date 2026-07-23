package com.debateseason_backend_v1.domain.chat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;
import com.debateseason_backend_v1.domain.chat.application.service.ChatServiceV1;
import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;

/**
 * v1.3.5 Phase 3a — 쓰기 라우팅 검증.
 *
 * 발신 대상 방의 종류에 따라 (chat_room_id = 컨테이너, thread_id = 스레드) 로 올바르게
 * 저장되는지 고정한다. saveMessage 는 chatRepository / chatRoomService 만 사용하므로
 * 나머지 협력자는 주입하지 않는다(null).
 */
class ChatThreadRoutingTest {

	private final ChatRepository chatRepository = mock(ChatRepository.class);
	private final ChatRoomServiceV1 chatRoomService = mock(ChatRoomServiceV1.class);
	private final ChatServiceV1 chatService =
		new ChatServiceV1(chatRepository, chatRoomService, null, null, null, null, null);

	private ChatEntity captureSaved() {
		ArgumentCaptor<ChatEntity> captor = ArgumentCaptor.forClass(ChatEntity.class);
		verify(chatRepository).save(captor.capture());
		return captor.getValue();
	}

	private ChatMessageRequest request(Long roomId, Long threadId) {
		return ChatMessageRequest.builder()
			.roomId(roomId)
			.threadId(threadId)
			.messageType(MessageType.CHAT)
			.content("메시지")
			.timeStamp(LocalDateTime.now())
			.build();
	}

	@Test
	@DisplayName("옛 방(THREAD)으로 보내면 컨테이너에 저장되고 그 방이 thread_id 가 된다 (구 앱 경로)")
	void routesThreadToContainer() {
		// given
		ChatRoom thread = ChatRoom.builder()
			.id(5L).roomType(ChatRoomType.THREAD).containerRoomId(100L).build();
		ChatRoom container = ChatRoom.builder()
			.id(100L).roomType(ChatRoomType.CONTAINER).build();
		given(chatRoomService.findChatRoomById(5L)).willReturn(thread);
		given(chatRoomService.findChatRoomById(100L)).willReturn(container);
		given(chatRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

		// when — 구 앱은 threadId 를 안 보낸다
		chatService.saveMessage(request(5L, null));

		// then
		ChatEntity saved = captureSaved();
		assertThat(saved.getChatRoomId().getId()).isEqualTo(100L); // 컨테이너
		assertThat(saved.getThreadId()).isEqualTo(5L);             // 옛 방 = 스레드
	}

	@Test
	@DisplayName("컨테이너로 보내면 지정한 threadId 로 저장된다 (신 웹 클라이언트)")
	void routesContainerWithThread() {
		// given
		ChatRoom container = ChatRoom.builder()
			.id(100L).roomType(ChatRoomType.CONTAINER).build();
		given(chatRoomService.findChatRoomById(100L)).willReturn(container);
		given(chatRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

		// when — 웹이 특정 주제 탭에서 발신
		chatService.saveMessage(request(100L, 7L));

		// then
		ChatEntity saved = captureSaved();
		assertThat(saved.getChatRoomId().getId()).isEqualTo(100L);
		assertThat(saved.getThreadId()).isEqualTo(7L);
	}

	@Test
	@DisplayName("이관 전 레거시 방(room_type=NULL)은 그대로 저장된다 (기존 동작 무회귀)")
	void legacyRoomUnchanged() {
		// given
		ChatRoom legacy = ChatRoom.builder().id(42L).build(); // roomType null
		given(chatRoomService.findChatRoomById(42L)).willReturn(legacy);
		given(chatRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

		// when
		chatService.saveMessage(request(42L, null));

		// then
		ChatEntity saved = captureSaved();
		assertThat(saved.getChatRoomId().getId()).isEqualTo(42L);
		assertThat(saved.getThreadId()).isNull();
	}
}
