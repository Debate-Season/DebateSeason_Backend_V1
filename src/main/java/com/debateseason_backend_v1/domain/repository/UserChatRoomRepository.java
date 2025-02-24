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
	List<UserChatRoom> findByChatRoom(ChatRoom chatRoom);
	UserChatRoom findByUserAndChatRoom(User user, ChatRoom chatRoom);
	UserChatRoom findByUserIdAndChatRoomId(Long userId,Long chatroomId);

	// 1. userId를 이용해서 해당 user가 투표한 토론방의 id값들 조회하기
	@Query(value = """
    SELECT cr.chat_room_id 
    FROM chat_room cr,
        (SELECT chat_room_id 
         FROM user_chat_room 
         WHERE user_id = :userId
         ORDER BY chat_room_id DESC
         LIMIT 2 OFFSET :page
        ) ucr
    WHERE cr.chat_room_id = ucr.chat_room_id
    """, nativeQuery = true)
	List<Long> findChatRoomsByPage(@Param("userId") Long userId,
		@Param("page") int page);

	
	// 1-1. 1.에서 가져온 토론방의 id값들로 해당 user가 투표한 토론방 여러개 조회하기.
	// AGREE, DISAGREE, chat_room_id, title, content, created_at 순으로 가져오기
	@Query(value = """
        SELECT 
            COUNT(CASE WHEN ucr.opinion = 'AGREE' THEN 1 END) AS AGREE,
            COUNT(CASE WHEN ucr.opinion = 'DISAGREE' THEN 1 END) AS DISAGREE,
            cr.chat_room_id ,cr.title, cr.content, cr.created_at 
        FROM user_chat_room ucr
        JOIN chat_room cr ON ucr.chat_room_id = cr.chat_room_id
        WHERE ucr.chat_room_id IN :chatRoomIds
        GROUP BY ucr.chat_room_id
        ORDER BY ucr.chat_room_id DESC
        """, nativeQuery = true)
	List<Object[]> findChatRoomByChatRoomIds(@Param("chatRoomIds") List<Long> chatRoomIds);

	@Query(value = """
    SELECT ch1.chat_room_id 
    FROM chat_room ch1, 
    (
        SELECT chat_room_id 
        FROM chat_room 
        WHERE issue_id = :issueId
        ORDER BY chat_room_id DESC
        LIMIT 2 OFFSET :page
    ) ch2
    WHERE ch1.chat_room_id = ch2.chat_room_id
    ORDER BY ch1.chat_room_id DESC
    """, nativeQuery = true)
	List<Long> findChatRoomsByIssueId(
		@Param("issueId") Long issueId,
		@Param("page")Integer page);

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

	@Query(value = """
    SELECT chat_room_id, opinion AS opinion
    FROM user_chat_room
    WHERE user_id = :userId AND chat_room_id IN (:chatRoomIds)
    ORDER BY chat_room_id DESC
    """, nativeQuery = true)
	List<Object[]> findUserChatRoomOpinions(@Param("userId") Long userId, @Param("chatRoomIds") List<Long> chatRoomIds);
}
