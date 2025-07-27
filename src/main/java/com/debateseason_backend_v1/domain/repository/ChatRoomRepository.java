package com.debateseason_backend_v1.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	// 1.Issue로 채팅방 가져오기
	List<ChatRoom> findByIssueEntity(IssueEntity issueEntity);

	Long countByIssueEntity(IssueEntity issueEntity);


	// 2-1 이슈방 issue-id와 관련된 채팅방ID 가져오기
	@Query(value = "SELECT chat_room_id FROM chat_room WHERE issue_id = :issueId ORDER BY chat_room_id DESC LIMIT 3", nativeQuery = true)
	List<Long> findTop3ChatRoomIdsByIssueId(@Param("issueId") Long issueId);
	// 2-2 이슈방 issue-id와 관련된 채팅방ID + 커서기반
	@Query(value = "SELECT chat_room_id FROM chat_room WHERE issue_id = :issueId AND chat_room_id < :ChatRoomId ORDER BY chat_room_id DESC LIMIT 3", nativeQuery = true)
	List<Long> findTop3ChatRoomIdsByIssueIdAndChatRoomId(
		@Param("issueId") Long issueId,
		@Param("ChatRoomId") Long ChatRoomId
	);

	// 3. 채팅방 여러 건 가져오기 + 찬성/반대 포함
	@Query(value = """
    SELECT ch.chat_room_id, ch.title, ch.content, ch.created_at,
           COUNT(CASE WHEN ucr.opinion = 'AGREE' THEN 1 END) AS AGREE,
           COUNT(CASE WHEN ucr.opinion = 'DISAGREE' THEN 1 END) AS DISAGREE
    FROM chat_room ch
         LEFT JOIN user_chat_room ucr ON ch.chat_room_id = ucr.chat_room_id
    WHERE ch.chat_room_id IN (:chatRoomIds)
    GROUP BY ch.chat_room_id, ch.title, ch.content, ch.created_at
    ORDER BY ch.chat_room_id DESC
    """, nativeQuery = true)
	List<Object[]> findChatRoomAggregates(@Param("chatRoomIds") List<Long> chatRoomIds);



	// 2. Legacy 인기 토론방 5개
	@Query(value = """
    SELECT iss.issue_id, iss.title,  
           chatroom.chat_room_id, chatroom.title
    FROM issue iss
    INNER JOIN (
        SELECT cr.chat_room_id, cr.title, cr.issue_id
        FROM chat_room cr
        INNER JOIN (
            SELECT chat_room_id
            FROM (
                SELECT chat_room_id, COUNT(chat_room_id) AS chats 
                FROM chat
                WHERE time_stamp <= NOW()
                GROUP BY chat_room_id
            ) tmp
            ORDER BY tmp.chats DESC
            LIMIT 5
        ) tmp2 ON cr.chat_room_id = tmp2.chat_room_id
    ) chatroom ON iss.issue_id = chatroom.issue_id
""", nativeQuery = true)
	List<Object[]> findTop5ActiveChatRooms();


	// 2. fix : 인기 토론방 5개
	// chat_room_id, title, created_at, AGREE, DISAGREE
	@Query(value = """
    SELECT 
        ucr.chat_room_id,
        tmp3.title,
        tmp3.created_at,
        COUNT(CASE WHEN ucr.opinion = 'AGREE' THEN 1 END) AS AGREE,
        COUNT(CASE WHEN ucr.opinion = 'DISAGREE' THEN 1 END) AS DISAGREE
    FROM user_chat_room ucr
    INNER JOIN (
        SELECT 
            cr.chat_room_id, 
            cr.title,
            tmp2.chats,
            cr.created_at
        FROM chat_room cr
        INNER JOIN (
            SELECT 
                chat_room_id, 
                COUNT(chat_room_id) AS chats 
            FROM chat
            WHERE time_stamp <= NOW()
            GROUP BY chat_room_id
            ORDER BY chats DESC
            LIMIT 5       
        ) tmp2 
        ON cr.chat_room_id = tmp2.chat_room_id
    ) tmp3
    ON ucr.chat_room_id = tmp3.chat_room_id
    GROUP BY ucr.chat_room_id, tmp3.title, tmp3.created_at, tmp3.chats
    ORDER BY tmp3.chats DESC
    """, nativeQuery = true)
	List<Object[]> findTop5ChatRoomsOrderedByChatCounts();



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
