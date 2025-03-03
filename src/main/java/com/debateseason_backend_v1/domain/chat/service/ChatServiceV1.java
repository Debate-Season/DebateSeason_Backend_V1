package com.debateseason_backend_v1.domain.chat.service;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chat.model.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chat.model.response.ChatListResponse;
import com.debateseason_backend_v1.domain.chat.model.response.ChatMessageResponse;
import com.debateseason_backend_v1.domain.chat.model.response.ChatMessagesByDate;
import com.debateseason_backend_v1.domain.chat.model.response.ChatMessagesResponse;
import com.debateseason_backend_v1.domain.chat.valide.ChatValidate;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.domain.repository.ChatRepository;
import com.debateseason_backend_v1.domain.repository.entity.Chat;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceV1 {

	private static final int PAGE_SIZE = 20;
	
	private final ChatRepository chatRepository;
	private final ChatRoomServiceV1 chatRoomService;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatValidate chatValidate;
	private final ApplicationEventPublisher eventPublisher;

	// ---------- WebSocket 실시간 메시지 처리 ----------
	
	public ChatMessageResponse processChatMessage(Long roomId, ChatMessageRequest message) {
		chatValidate.validateMessageLength(message);
		
		enrichChatMessage(message, roomId);
		eventPublisher.publishEvent(message);  // 비동기 저장을 위한 이벤트 발행
		return ChatMessageResponse.from(message);
	}

	private void enrichChatMessage(ChatMessageRequest message, Long roomId) {
		message.setRoomId(roomId);
		message.setMessageType(MessageType.CHAT);
		message.setTimeStamp(LocalDateTime.now());
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
	public ApiResult<ChatMessagesResponse> getChatMessages(Long roomId, Long cursor) {
		cursor = (cursor == null) ? Long.MAX_VALUE : cursor;
		List<ChatMessagesByDate> messagesByDate = collectLastWeekMessages(roomId, cursor, LocalDate.now());
		
		ChatMessagesResponse response = ChatMessagesResponse.builder()
				.messagesByDates(messagesByDate)
				.nextCursor(getLastMessageId(messagesByDate))
				.build();

		return ApiResult.success("채팅 메시지를 성공적으로 조회했습니다.", response);
	}

	private List<ChatMessagesByDate> collectLastWeekMessages(Long roomId, Long cursor, LocalDate startDate) {
		ArrayList<ChatMessagesByDate> messagesByDate = new ArrayList<>();
		LocalDate currentDate = startDate;

		for (int i = 0; i < 7; i++) {
			processDateMessages(roomId, cursor, currentDate, messagesByDate);
			currentDate = currentDate.minusDays(1);
		}

		return messagesByDate;
	}

	private void processDateMessages(Long roomId, Long cursor, LocalDate date, 
								   List<ChatMessagesByDate> messagesByDate) {
		List<Chat> chats = chatRepository.findByRoomIdAndCursorAndDate(
				roomId, cursor, date, PageRequest.of(0, PAGE_SIZE + 1));

		if (!chats.isEmpty()) {
			boolean hasMore = chats.size() > PAGE_SIZE;
			List<Chat> displayChats = hasMore ? chats.subList(0, PAGE_SIZE) : chats;
			
			messagesByDate.add(createChatMessagesByDate(roomId, date, displayChats, hasMore));
		}
	}

	private ChatMessagesByDate createChatMessagesByDate(Long roomId, LocalDate date, 
													  List<Chat> chats, boolean hasMore) {
		List<ChatMessageResponse> messages = chats.stream()
				.map(this::convertToChatMessageResponse)
				.collect(Collectors.toList());

		return ChatMessagesByDate.builder()
				.date(date.toString())
				.chatMessageResponses(messages)
				.hasMore(hasMore)
				.totalCount(chatRepository.countByRoomIdAndDate(roomId, date))
				.build();
	}

	private ChatMessageResponse convertToChatMessageResponse(Chat chat) {
		return ChatMessageResponse.builder()
				.roomId(chat.getId())
				.messageType(chat.getMessageType())
				.content(chat.getContent())
				.sender(chat.getSender())
				.opinionType(chat.getOpinionType())
				.userCommunity(chat.getUserCommunity())
				.timeStamp(chat.getTimeStamp())
				.build();
	}

	private String getLastMessageId(List<ChatMessagesByDate> messagesByDate) {
		if (messagesByDate.isEmpty() || 
			messagesByDate.get(0).getChatMessageResponses().isEmpty()) {
			return null;
		}
		return messagesByDate.get(0).getChatMessageResponses()
				.get(messagesByDate.get(0).getChatMessageResponses().size() - 1)
				.getRoomId()
				.toString();
	}
}
