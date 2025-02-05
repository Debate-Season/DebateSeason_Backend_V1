package com.debateseason_backend_v1.domain.chat.service;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chat.model.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.chat.model.response.ChatListResponse;
import com.debateseason_backend_v1.domain.chat.model.response.ChatMessageResponse;
import com.debateseason_backend_v1.domain.chat.model.response.ChatMessagesByDate;
import com.debateseason_backend_v1.domain.chat.model.response.ChatMessagesResponse;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.domain.repository.ChatRepository;
import com.debateseason_backend_v1.domain.repository.entity.Chat;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceV1 {

	private final ChatRepository chatRepository;
	private final ChatRoomServiceV1 chatRoomService;

	//채팅 리스트 가져오기 메서드
	private static final int PAGE_SIZE = 20;
	@Transactional(readOnly = true)
	public ApiResult<ChatMessagesResponse> getChatMessages(Long roomId, Long cursor) {
		//초기 커서가 없으면 가장 큰 값으로 설정
		cursor = (cursor == null) ? Long.MAX_VALUE : cursor;
		LocalDate today = LocalDate.now();

		List<ChatMessagesByDate> messagesByDate = collectLastWeekMessages(roomId, cursor, today);
		String lastMessageId = getLastMessageId(messagesByDate);

		ChatMessagesResponse response = ChatMessagesResponse.builder()
				.messagesByDates(messagesByDate)
				.nextCursor(lastMessageId)
				.build();

		return ApiResult.success("채팅 메시지를 성공적으로 조회했습니다.", response);
	}


	//최근 7일 데이터 가져오기
	private List<ChatMessagesByDate> collectLastWeekMessages(Long roomId, Long cursor, LocalDate startDate) {
		ArrayList<ChatMessagesByDate> messagesByDate = new ArrayList<>();
		LocalDate currentDate = startDate;

		// 7일치 데이터 조회
		for (int i = 0; i < 7; i++) {
			List<Chat> chats = chatRepository.findByRoomIdAndCursorAndDate(
					roomId,
					cursor,
					currentDate,
					PageRequest.of(0, PAGE_SIZE + 1)  // 다음 페이지 존재 여부 확인을 위해 1개 더 조회
			);

			if (!chats.isEmpty()) {
				boolean hasMore = chats.size() > PAGE_SIZE;
				// PAGE_SIZE + 1개를 조회했으므로, 실제 표시할 메시지는 PAGE_SIZE개까지만
				List<Chat> displayChats = hasMore ? chats.subList(0, PAGE_SIZE) : chats;

				List<ChatMessageResponse> chatMessages = displayChats.stream()
						.map(this::convertToChatMessageResponse)
						.collect(Collectors.toList());

				long totalCount = chatRepository.countByRoomIdAndDate(roomId, currentDate);

				messagesByDate.add(ChatMessagesByDate.builder()
						.date(currentDate.toString())
						.chatMessageResponses(chatMessages)
						.hasMore(hasMore)
						.totalCount(totalCount)
						.build());
			}
			currentDate = currentDate.minusDays(1);
		}

		return messagesByDate;
	}

	private String getLastMessageId(List<ChatMessagesByDate> messagesByDate) {
		if (messagesByDate.isEmpty() || messagesByDate.get(0).getChatMessageResponses().isEmpty()) {
			return null;
		}
		return messagesByDate.get(0).getChatMessageResponses()
				.get(messagesByDate.get(0).getChatMessageResponses().size() - 1)
				.getId()
				.toString();
	}
	private ChatMessageResponse convertToChatMessageResponse(Chat chat) {
		return ChatMessageResponse.builder()
				.id(chat.getId())           // Chat 엔티티의 ID를 설정
				.messageType(chat.getMessageType())
				.content(chat.getContent())
				.sender(chat.getSender())
				.opinionType(chat.getOpinionType())
				.userCommunity(chat.getUserCommunity())
				.timeStamp(chat.getTimeStamp())
				.build();
	}
	//채팅 저장 메서드
	@Transactional
	public void saveMessage(ChatMessageRequest chatMessage) {

		ChatRoom foundChatRoom = chatRoomService.findChatRoomById(chatMessage.getRoomId());

		Chat chat = Chat.builder()
				.chatRoomId(foundChatRoom)
				.sender(chatMessage.getSender())
				.content(chatMessage.getContent())
				.messageType(chatMessage.getMessageType())
				.opinionType(chatMessage.getOpinionType())
				.userCommunity(chatMessage.getUserCommunity())
				.timeStamp(chatMessage.getTimeStamp())
				.build();

		chatRepository.save(chat);
		log.debug("채팅 메시지 저장 완료: roomId={}, sender={}", chatMessage.getRoomId(), chatMessage.getSender());
	}


}
