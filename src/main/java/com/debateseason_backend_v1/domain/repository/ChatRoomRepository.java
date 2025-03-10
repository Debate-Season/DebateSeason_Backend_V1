package com.debateseason_backend_v1.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.entity.Issue;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	// 1.Issue로 채팅방 가져오기
	List<ChatRoom> findByIssue(Issue issue);

	Long countByIssue(Issue issue);




	// 인기 토론방 5개
		@Query(value = """
        SELECT cr.chat_room_id, cr.title, cr.content 
        FROM chat_room cr,
        (
            SELECT chat_room_id, chats FROM
            (
                SELECT chat_room_id, COUNT(chat_room_id) AS chats 
                FROM chat
                WHERE time_stamp <= NOW() AND time_stamp >= DATE(NOW())
                GROUP BY chat_room_id
            ) tmp
            ORDER BY tmp.chats DESC
            LIMIT 5
        ) tmp2
        WHERE cr.chat_room_id = tmp2.chat_room_id
        """, nativeQuery = true)
		List<Object[]> findTop5ActiveChatRooms();



}
