package com.debateseason_backend_v1.domain.repository;

import com.debateseason_backend_v1.domain.chatroom.model.ChatRoom;
import com.debateseason_backend_v1.domain.issue.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

    // 1.Issue로 채팅방 가져오기
    List<ChatRoom> findByIssue(Issue issue);
}
