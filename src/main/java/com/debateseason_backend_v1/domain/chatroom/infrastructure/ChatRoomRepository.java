package com.debateseason_backend_v1.domain.chatroom.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.entity.Issue;

public interface ChatRoomRepository {
	// 기본적인 CRUD
	void save(ChatRoom chatRoom);

	ChatRoom findById(Long chatRoomId);



	// 1.Issue로 채팅방 가져오기
	List<ChatRoom> findByIssue(Issue issue);

	Long countByIssue(Issue issue);

	List<Object[]> findTop5ActiveChatRooms();


	/*
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

	 */


	// 1. 토론방에서 "합계,논리,태도" 부분
	List<Object[]> getReactionSummaryByOpinion(
		Long chatRoomId,
		String opinion
	);

	// 1-1. "MVP" 출력

	String findTopChatRoomUserNickname(
		Long chatRoomId,
		String opinion
	);

	// 내 하이라이트 가져오기
	List<Object[]> findChatHighlight(
		Long userId,
		Long chatRoomId
	);
}
