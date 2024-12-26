package com.debateseason_backend_v1.domain.chat.service;

import com.debateseason_backend_v1.common.enums.OpinionType;
import com.debateseason_backend_v1.domain.chat.model.ChatMessage;
import com.debateseason_backend_v1.domain.chat.model.response.ChatListResponse;
import com.debateseason_backend_v1.domain.repository.entity.Chat;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.ChatRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ChatServiceV1 {
	
	public ChatListResponse findChatsBetweenUsers(String from, String to) {
		//TODO : ERD 확정 되면 구현 - ksb
		return null;
	}

}
