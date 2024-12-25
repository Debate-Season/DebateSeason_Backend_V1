package com.debateseason_backend_v1.domain.chat.service;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.domain.chat.model.response.ChatListResponse;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ChatServiceV1 {
	
	public ChatListResponse findChatsBetweenUsers(String from, String to) {
		//TODO : ERD 확정 되면 구현 - ksb
		return null;
	}

}
