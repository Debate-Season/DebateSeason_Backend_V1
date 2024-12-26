package com.debateseason_backend_v1.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.entity.Issue;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	// 1.Issue로 채팅방 가져오기
	List<ChatRoom> findByIssue(Issue issue);

	Long countByIssue(Issue issue);
}
