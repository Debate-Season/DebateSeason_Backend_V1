package com.debateseason_backend_v1.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.repository.entity.UserChatRoom;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
	List<UserChatRoom> findByChatRoom(
		ChatRoom chatRoom
	);
	UserChatRoom findByUserAndChatRoom(
		User user,
		ChatRoom chatRoom
	);
	UserChatRoom findByUserIdAndChatRoomId(
		Long userId,Long
		chatroomId
	);

	// 1-1 Paramter가 없는 경우
	@Query(value = "SELECT chat_room_id FROM user_chat_room WHERE user_id = :userId ORDER BY chat_room_id DESC LIMIT :size", nativeQuery = true)
	List<Long> findTopChatRoomIdsByUserId(
		@Param("userId") Long userId,
		@Param("size") int size
	);


	// 1-2. Parameter가 있는 경우
	@Query(value = "SELECT chat_room_id FROM user_chat_room WHERE user_id = :userId AND chat_room_id < :ChatRoomId ORDER BY chat_room_id DESC LIMIT :size", nativeQuery = true)
	List<Long> findTopChatRoomIdsByUserIdAndChatRoomId(
		@Param("userId") Long userId,
		@Param("ChatRoomId") Long ChatRoomId,
		@Param("size") int size
	);

	// refesh 쿼리
	@Query(value = """
        SELECT
            ucr3.AGREE,
            ucr3.DISAGREE,
            ch.chat_room_id,
            ch.title,
            ch.content,
            ch.created_at,
            ucr3.opinion
        FROM chat_room ch
        INNER JOIN (
            SELECT ucr2.chat_room_id, ucr2.opinion, ucr1.AGREE, ucr1.DISAGREE
            FROM (
                SELECT
                    ucr.chat_room_id,
                    COUNT(CASE WHEN ucr.opinion = 'AGREE' THEN 1 END) AS AGREE,
                    COUNT(CASE WHEN ucr.opinion = 'DISAGREE' THEN 1 END) AS DISAGREE
                FROM user_chat_room ucr
                WHERE chat_room_id IN (:chatRoomIds)
                GROUP BY ucr.chat_room_id
            ) ucr1
            INNER JOIN (
                SELECT chat_room_id, opinion
                FROM user_chat_room
                WHERE user_id = :userId
            ) ucr2
            ON ucr1.chat_room_id = ucr2.chat_room_id
        ) ucr3
        ON ch.chat_room_id = ucr3.chat_room_id
        ORDER BY ch.created_at DESC
        """, nativeQuery = true)
	List<Object[]> findChatRoomWithOpinions(
		@Param("userId") Long userId,
		@Param("chatRoomIds") List<Long> chatRoomIds
	);

	// 2-1 이슈방 issue-id로만 조회
	@Query(value = "SELECT chat_room_id FROM chat_room WHERE issue_id = :issueId ORDER BY chat_room_id DESC LIMIT :size", nativeQuery = true)
	List<Long> findTopChatRoomIdsByIssueIdWithSize(
		@Param("issueId") Long issueId,
		@Param("size") int size
	);

	// 2-2 이슈방 issue-id + 커서기반
	@Query(value = "SELECT chat_room_id FROM chat_room "
		+ "WHERE issue_id = :issueId AND chat_room_id < :ChatRoomId "
		+ "ORDER BY chat_room_id "
		+ "DESC LIMIT :size", nativeQuery = true)
	List<Long> findTopChatRoomIdsByIssueIdAndChatRoomIdWithSize(
		@Param("issueId") Long issueId,
		@Param("ChatRoomId") Long ChatRoomId,
		@Param("size") int size
	);
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
	List<Object[]> findChatRoomAggregates(
		@Param("chatRoomIds") List<Long> chatRoomIds
	);

	@Query(value = """
    SELECT chat_room_id, opinion AS opinion
    FROM user_chat_room
    WHERE user_id = :userId AND chat_room_id IN (:chatRoomIds)
    ORDER BY chat_room_id DESC
    """, nativeQuery = true)
	List<Object[]> findUserChatRoomOpinions(
		@Param("userId") Long userId,
		@Param("chatRoomIds") List<Long> chatRoomIds
	);
}
