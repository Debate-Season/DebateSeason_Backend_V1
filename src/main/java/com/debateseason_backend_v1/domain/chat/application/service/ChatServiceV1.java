package com.debateseason_backend_v1.domain.chat.application.service;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chat.application.ChatReactionRepository;
import com.debateseason_backend_v1.domain.chat.application.ChatRepository;
import com.debateseason_backend_v1.domain.chat.infrastructure.chat.Chat;
import com.debateseason_backend_v1.domain.chat.model.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chat.model.response.ChatMessageResponse;
import com.debateseason_backend_v1.domain.chat.model.response.ChatMessagesResponse;
import com.debateseason_backend_v1.domain.chat.valide.ChatValidate;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceV1 {

	private static final int PAGE_SIZE = 20;
	
	private final ChatRepository chatRepository;
	private final ChatRoomServiceV1 chatRoomService;
	private final ChatValidate chatValidate;
	private final ChatReactionRepository chatReactionRepository;

	// ---------- WebSocket 실시간 메시지 처리 ----------
	
	public ChatMessageResponse processChatMessage(Long roomId, ChatMessageRequest message, SimpMessageHeaderAccessor headerAccessor) {
		// 메시지 유효성 검사
		chatValidate.validateMessageLength(message);
		
		// 채팅방 존재 여부 확인
		ChatRoom chatRoom = chatRoomService.findChatRoomById(roomId);

		// 인증 정보에서 사용자 ID 가져오기
		Long userId = null;
		Principal principal = headerAccessor.getUser();
		if (principal != null) {
			userId = Long.valueOf(principal.getName());
		}

		// 메시지 저장
		Chat chat = Chat.from(message, chatRoom, userId);
		Chat savedchat = chatRepository.save(chat);
		log.debug("채팅 메시지 저장 완료: roomId={}, sender={}", roomId, message.getSender());

		// 빈 반응 정보를 포함한 응답 생성
		return ChatMessageResponse.from(savedchat);
	}

	public ChatMessageResponse processJoinMessage(ChatMessageRequest joinRequest) {
		return ChatMessageResponse.builder()
			.messageType(MessageType.JOIN)
			.sender(joinRequest.getSender())
			.content(joinRequest.getSender() + " joined!")
			.opinionType(joinRequest.getOpinionType())
			.userCommunity(joinRequest.getUserCommunity())
			.timeStamp(LocalDateTime.now())
			.build();
	}

	// ---------- 채팅 메시지 영속성 처리 ----------

	@Transactional
	public void saveMessage(ChatMessageRequest chatMessage) {
		ChatRoom chatRoom = chatRoomService.findChatRoomById(chatMessage.getRoomId());
		Chat chat = convertToEntity(chatMessage, chatRoom);
		
		chatRepository.save(chat);
		log.debug("채팅 메시지 저장 완료: roomId={}, sender={}", 
				chatMessage.getRoomId(), chatMessage.getSender());
	}

	private Chat convertToEntity(ChatMessageRequest message, ChatRoom chatRoom) {
		return Chat.builder()
				.chatRoomId(chatRoom)
				.sender(message.getSender())
				.content(message.getContent())
				.messageType(message.getMessageType())
				.opinionType(message.getOpinionType())
				.userCommunity(message.getUserCommunity())
				.timeStamp(message.getTimeStamp())
				.build();
	}

	// ---------- 채팅 메시지 조회 ----------

	@Transactional(readOnly = true)
	public ApiResult<ChatMessagesResponse> getChatMessages(Long roomId, Long cursor, Long userId) {
		cursor = (cursor == null) ? Long.MAX_VALUE : cursor;
		
		// 채팅방 존재 여부 확인
		chatRoomService.findChatRoomById(roomId);
		
		// 메시지 조회 (최대 20개 + 1개 더 조회하여 hasMore 확인)
		List<Chat> chats = chatRepository.findByRoomIdAndCursor(
				roomId, cursor, PageRequest.of(0, PAGE_SIZE + 1));
		
		boolean hasMore = chats.size() > PAGE_SIZE;
		List<Chat> displayChats = hasMore ? chats.subList(0, PAGE_SIZE) : chats;
		
		// 응답 생성
		List<ChatMessageResponse> messageResponses = displayChats.stream()
				.map(chat -> ChatMessageResponse.from(chat, userId, chatReactionRepository))
				.collect(Collectors.toList());
		
		String nextCursor = null;
		if (!displayChats.isEmpty()) {
			nextCursor = String.valueOf(displayChats.get(displayChats.size() - 1).getId());
		}
		
		int totalCount = chatRepository.countByRoomId(roomId);
		
		ChatMessagesResponse response = ChatMessagesResponse.builder()
				.items(messageResponses)
				.hasMore(hasMore)
				.nextCursor(nextCursor)
				.totalCount(totalCount)
				.build();
		
		return ApiResult.success("채팅 메시지를 성공적으로 조회했습니다.", response);
	}
}
