package com.debateseason_backend_v1.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.repository.entity.UserChatRoom;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
	List<UserChatRoom> findByChatRoom(ChatRoom chatRoom);

	UserChatRoom findByUserAndChatRoom(User user, ChatRoom chatRoom);
}
