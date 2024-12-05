package com.debateseason_backend_v1.domain.chat.service;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.domain.chat.model.response.ChatListResponse;
import com.debateseason_backend_v1.domain.repository.ChatRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ChatServiceV1 {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatRepository chatRepository;

	// 1. ChatMessage 저장은 STOMP 단에서 처리해야 하는 것처럼 보임.
	// 따라서, 일단 임시로 주석처리함. 추후 저장한다고 하면 -> 주석해지
	/*
	public ResponseEntity<?> save(ChatDTO chatDTO, Long chatRoomId) {

		// 1. chatRoom 조회하기
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(
				() -> new RuntimeException("There is no ChatRoom :" + chatRoomId)
			);

		// 2. Chat 엔티티 생성
		Chat chat = Chat.builder()
			.chatRoom(chatRoom)
			.sender(chatDTO.getSender())
			.category(chatDTO.getCategory())
			.content(chatDTO.getContent())
			.build();

		// 3. Chat 저장하기
		chatRepository.save(chat);

		return ResponseEntity.ok("Yes, Save Chat!");
	}

	 */

	public ChatListResponse findChatsBetweenUsers(String from, String to) {
		//TODO : ERD 확정 되면 구현 - ksb
		return null;
	}

}
