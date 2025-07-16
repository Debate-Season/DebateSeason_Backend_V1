package com.debateseason_backend_v1.domain.chatroom.infrastructure.manager;

import java.util.List;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomPaginationManager {

	private final ChatRoomRepository chatRoomRepository;

	// 1. 채팅방 페이지네이션, 채팅방ID 반환을 함.
	public List<Long> getChatRoomsByPage(Long issueId, Long ChatRoomId){

		List<Long> chatRoomIds;

		if (ChatRoomId == null) {
			chatRoomIds = chatRoomRepository.findTop3ChatRoomIdsByIssueId(issueId);
		} else {
			// 그 이후 페이지 , 페이지네이션 값은 chatRoomId. 왜냐하면 들어온 순으로 id가 순차적으로 저장이 됨.
			chatRoomIds = chatRoomRepository.findTop3ChatRoomIdsByIssueIdAndChatRoomId(issueId, ChatRoomId);
		}

		return chatRoomIds;

	}
}
