package com.debateseason_backend_v1.domain.chatroom.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.ChatRoomJpaRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.entity.Issue;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ChatRoomRepositoryImpl implements ChatRoomRepository{

	private final ChatRoomJpaRepository chatRoomJpaRepository;

	// 기본적인 CRUD

	@Override
	public void save(ChatRoom chatRoom) {
		chatRoomJpaRepository.save(chatRoom);
	}

	@Override
	public ChatRoom findById(Long chatRoomId) {
		return chatRoomJpaRepository.findById(chatRoomId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.NOT_FOUND_CHATROOM)
			)
			;
	}

	@Override
	public List<ChatRoom> findByIssue(Issue issue) {
		return chatRoomJpaRepository.findByIssue(issue);
	}

	@Override
	public Long countByIssue(Issue issue) {
		return chatRoomJpaRepository.countByIssue(issue);
	}

	@Override
	public List<Object[]> findTop5ActiveChatRooms() {
		return chatRoomJpaRepository.findTop5ActiveChatRooms();
	}

	@Override
	public List<Object[]> getReactionSummaryByOpinion(Long chatRoomId, String opinion) {
		return chatRoomJpaRepository.getReactionSummaryByOpinion(chatRoomId,opinion);
	}

	@Override
	public String findTopChatRoomUserNickname(Long chatRoomId, String opinion) {
		return chatRoomJpaRepository.findTopChatRoomUserNickname(chatRoomId,opinion);
	}

	@Override
	public List<Object[]> findChatHighlight(Long userId, Long chatRoomId) {
		return chatRoomJpaRepository.findChatHighlight(userId,chatRoomId);
	}
}
