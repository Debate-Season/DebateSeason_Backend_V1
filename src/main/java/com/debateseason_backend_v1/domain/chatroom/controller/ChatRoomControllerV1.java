package com.debateseason_backend_v1.domain.chatroom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.domain.chat.dto.ChatDTO;
import com.debateseason_backend_v1.domain.chat.service.ChatServiceV1;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDTO;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class ChatRoomControllerV1 {

	private final ChatRoomServiceV1 chatRoomServiceV1;
	private final ChatServiceV1 chatServiceV1;

	// 4. 채팅방(=안건=토론방)생성하기, title,content -> JSON, issue_id = 쿼리스트링
	@PostMapping("/room")
	public ResponseEntity<?> createChatRoom(@RequestBody ChatRoomDTO chatRoomDTO,
		@RequestParam(name = "issue-id") Long issue_id) {
		return chatRoomServiceV1.save(chatRoomDTO, issue_id);
	}

	// 4. 채팅방 단건 불러오기
	@GetMapping("/room")
	public ResponseEntity<?> getChatRoom(@RequestParam(name = "chatroom-id") Long chatRoomId) {
		return chatRoomServiceV1.fetch(chatRoomId);
	}

	// 5. 채팅방 찬성/반대 투표하기, opinion, chatroomid = 쿼리스트링
	@PostMapping("/room/vote")
	public ResponseEntity<?> voteChatRoom(@RequestParam(name = "opinion") String opinion,
		@RequestParam(name = "chatroom-id") Long chatRoomId) {
		return chatRoomServiceV1.vote(opinion, chatRoomId);
	}

	// 6. 채팅메시지 발송
	// 쿼리스트링은 chatRoomId
	@PostMapping("/room/send")
	public ResponseEntity<?> sendChat(@RequestBody ChatDTO chatDTO,
		@RequestParam(name = "chatroom-id") Long chatRoomId) {
		return chatServiceV1.save(chatDTO, chatRoomId);
	}
}
