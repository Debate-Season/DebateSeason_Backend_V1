package com.debateseason_backend_v1.domain.repository;

import com.debateseason_backend_v1.domain.chat.model.Chat;
import com.debateseason_backend_v1.domain.chatroom.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat,Long> {

    // 1. 채팅방 관련 채팅들 불러오기
    List<Chat> findByChatRoom(ChatRoom chatRoom);
}
