package com.debateseason_backend_v1.domain.chat.service;

import com.debateseason_backend_v1.domain.chat.model.ChatMessage;
import com.debateseason_backend_v1.domain.chat.model.response.ChatListResponse;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.domain.repository.ChatRepository;
import com.debateseason_backend_v1.domain.repository.entity.Chat;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceV1 {

	private final ChatRepository chatRepository;
	private final ChatRoomServiceV1 chatRoomService;

	public ChatListResponse findChatsBetweenUsers(String from, String to) {
		//TODO : ERD 확정 되면 구현 - ksb
		return null;
	}

	@Transactional
	public void saveMessage(ChatMessage chatMessage) {

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
