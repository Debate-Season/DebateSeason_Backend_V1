package com.debateseason_backend_v1.domain.repository;

import com.debateseason_backend_v1.domain.repository.entity.Chat;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat,Long> {

}
