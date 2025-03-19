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


	// 1. 토론방에서 "합계,논리,태도" 부분
	@Query(value = """
    SELECT SUM(s.LOGIC) AS logic, SUM(s.ATTITUDE) AS attitude
    FROM (
        SELECT COUNT(CASE WHEN reaction_type = 'LOGIC' THEN 1 END) AS LOGIC,
               COUNT(CASE WHEN reaction_type = 'ATTITUDE' THEN 1 END) AS ATTITUDE
        FROM (
            SELECT chat_id FROM chat WHERE chat_room_id = :chatRoomId AND opinion_type = :opinion
        ) ch
        JOIN chat_reaction ch_r ON ch.chat_id = ch_r.chat_id
        GROUP BY ch.chat_id
    ) s
    """, nativeQuery = true)
	List<Object[]> getReactionSummaryByOpinion(
		@Param("chatRoomId") Long chatRoomId,
		@Param("opinion") String opinion
	);

	// 1-1. "MVP" 출력
	@Query(value = """
    SELECT p.nickname FROM profile p WHERE p.user_id = (
        SELECT c.user_id FROM chat c WHERE c.chat_id = (
            SELECT s.chat_id FROM (
                SELECT
                    ch_r.chat_id,
                    COUNT(CASE WHEN ch_r.reaction_type = 'LOGIC' THEN 1 END) +
                    COUNT(CASE WHEN ch_r.reaction_type = 'ATTITUDE' THEN 1 END) AS score
                FROM chat ch
                JOIN chat_reaction ch_r ON ch.chat_id = ch_r.chat_id
                WHERE ch.chat_room_id = :chatRoomId AND ch.opinion_type = :opinion
                GROUP BY ch.chat_id
            ) s
            ORDER BY s.score DESC
            LIMIT 1
        )
    )
    """, nativeQuery = true)
	String findTopChatRoomUserNickname(
		@Param("chatRoomId") Long chatRoomId,
		@Param("opinion") String opinion);

	// 내 하이라이트 가져오기
	@Query(value = """
    SELECT ch.chat_id, ch.content,
        COUNT(CASE WHEN ch_r.reaction_type = 'LOGIC' THEN 1 END) AS logic,
        COUNT(CASE WHEN ch_r.reaction_type = 'ATTITUDE' THEN 1 END) AS attitude
    FROM (
        SELECT * FROM chat 
        WHERE user_id = :userId AND chat_room_id = :chatRoomId
    ) ch
    JOIN chat_reaction ch_r ON ch.chat_id = ch_r.chat_id
    GROUP BY ch.chat_id, ch.content
    """, nativeQuery = true)
	List<Object[]> findChatHighlight(@Param("userId") Long userId, @Param("chatRoomId") Long chatRoomId);

}
