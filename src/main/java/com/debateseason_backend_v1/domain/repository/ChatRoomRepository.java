package com.debateseason_backend_v1.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	// 1.Issue로 채팅방 가져오기
	List<ChatRoom> findByIssueEntity(IssueEntity issueEntity);

	Long countByIssueEntity(IssueEntity issueEntity);

	// v1.3.5 (Phase 3b-endpoint): 이슈의 컨테이너 방 1건.
	// 이관 후 이슈당 컨테이너 1개가 존재하지만, 컨테이너가 없는 이슈(방이 없던 이슈 등)도 있을 수 있어 Optional.
	Optional<ChatRoom> findFirstByIssueEntity_IdAndRoomType(Long issueId, ChatRoomType roomType);

	// v1.3.5 (Phase 3b-endpoint): 이슈의 스레드(=옛 방) id 목록.
	// 컨테이너 제외·레거시(NULL)는 스레드로 취급 — findTop3ChatRoomIdsByIssueId 와 동일 규칙(단 LIMIT 없음).
	@Query(value = "SELECT chat_room_id FROM chat_room WHERE issue_id = :issueId AND (room_type IS NULL OR room_type <> 'CONTAINER') ORDER BY chat_room_id DESC", nativeQuery = true)
	List<Long> findThreadRoomIdsByIssueId(@Param("issueId") Long issueId);


	// 2-1 이슈방 issue-id와 관련된 채팅방ID 가져오기
	// v1.3.5: 컨테이너는 목록에서 제외(스레드=옛 방만 노출). 레거시(NULL)도 스레드로 취급.
	@Query(value = "SELECT chat_room_id FROM chat_room WHERE issue_id = :issueId AND (room_type IS NULL OR room_type <> 'CONTAINER') ORDER BY chat_room_id DESC LIMIT 3", nativeQuery = true)
	List<Long> findTop3ChatRoomIdsByIssueId(@Param("issueId") Long issueId);
	// 2-2 이슈방 issue-id와 관련된 채팅방ID + 커서기반
	@Query(value = "SELECT chat_room_id FROM chat_room WHERE issue_id = :issueId AND chat_room_id < :ChatRoomId AND (room_type IS NULL OR room_type <> 'CONTAINER') ORDER BY chat_room_id DESC LIMIT 3", nativeQuery = true)
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



	// 2. 실시간 핫한 토론 5개 — 시간창 캐스케이드
	//
	// 30분 → 8시간 → 24시간 → 72시간 순으로 칸을 채우고, 그래도 남으면 최근 생성순이다.
	// 단계 우선이 절대적이다: 30분 창에 1건인 방이 8시간 창에 50건인 방보다 위다.
	// 창 경계는 RankingWindow 가 계산해서 넘긴다 (모두 같은 :bucketEnd 에 앵커된 누적 구간).
	//
	// 창이 중첩이라 4번 조인할 필요 없이 조건부 집계 한 번이면 된다.
	// 서브쿼리 WHERE 가 이미 72시간으로 잘라두므로 c72h 는 COUNT(*) 와 같다.
	//
	// 첫 CASE 가 단계, 둘째 CASE 가 그 단계에서의 카운트다.
	// 5단계(최근 생성순)는 tier=4 / count=0 이라 꼬리의 created_at 정렬이 그대로 처리한다.
	//
	// 정렬·LIMIT 은 반드시 최외곽에 있어야 한다. 파생 테이블 안에서 자르면
	// (1) MySQL 이 파생 테이블의 정렬을 바깥까지 보장하지 않아 응답 순서가 흐트러지고
	// (2) 바깥 필터·조인에 걸러져 5개 미만으로 떨어진다.
	//
	// created_at 까지 같은 방들이 실제로 있다(시드로 한 번에 만든 87~90, 84~86, 79~83).
	// 그래서 chat_room_id 를 마지막 키로 둬야 순서가 확정된다. 없으면 새로고침마다 섞인다.
	@Query(value = """
    SELECT iss.issue_id, iss.title,
           cr.chat_room_id, cr.title
    FROM chat_room cr
    INNER JOIN issue iss ON iss.issue_id = cr.issue_id
    LEFT JOIN (
        -- v1.3.5: 이관 전/후 모두 주제 단위 랭킹 (post: thread_id / pre: chat_room_id)
        SELECT COALESCE(thread_id, chat_room_id) AS rid,
               SUM(CASE WHEN time_stamp >= :window30m THEN 1 ELSE 0 END) AS c30m,
               SUM(CASE WHEN time_stamp >= :window8h  THEN 1 ELSE 0 END) AS c8h,
               SUM(CASE WHEN time_stamp >= :window24h THEN 1 ELSE 0 END) AS c24h,
               COUNT(*)                                                  AS c72h
        FROM chat
        WHERE time_stamp >= :window72h AND time_stamp < :bucketEnd
        GROUP BY COALESCE(thread_id, chat_room_id)
    ) tmp ON tmp.rid = cr.chat_room_id
    WHERE cr.room_type IS NULL OR cr.room_type <> 'CONTAINER'
    ORDER BY
        CASE WHEN COALESCE(tmp.c30m, 0) > 0 THEN 0
             WHEN COALESCE(tmp.c8h,  0) > 0 THEN 1
             WHEN COALESCE(tmp.c24h, 0) > 0 THEN 2
             WHEN COALESCE(tmp.c72h, 0) > 0 THEN 3
             ELSE 4 END ASC,
        CASE WHEN COALESCE(tmp.c30m, 0) > 0 THEN tmp.c30m
             WHEN COALESCE(tmp.c8h,  0) > 0 THEN tmp.c8h
             WHEN COALESCE(tmp.c24h, 0) > 0 THEN tmp.c24h
             WHEN COALESCE(tmp.c72h, 0) > 0 THEN tmp.c72h
             ELSE 0 END DESC,
        cr.created_at DESC,
        cr.chat_room_id DESC
    LIMIT 5
""", nativeQuery = true)
	List<Object[]> findTop5ActiveChatRooms(
		@Param("window30m") LocalDateTime window30m,
		@Param("window8h") LocalDateTime window8h,
		@Param("window24h") LocalDateTime window24h,
		@Param("window72h") LocalDateTime window72h,
		@Param("bucketEnd") LocalDateTime bucketEnd
	);


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
            -- v1.3.5: 이관 전/후 모두 주제 단위 랭킹 (post: thread_id / pre: chat_room_id).
            -- 컨테이너는 votes(user_chat_room)가 없어 아래 INNER JOIN 에서 자연 제외된다.
            SELECT
                COALESCE(thread_id, chat_room_id) AS chat_room_id,
                COUNT(*) AS chats
            FROM chat
            WHERE time_stamp <= NOW()
            GROUP BY COALESCE(thread_id, chat_room_id)
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
            SELECT chat_id FROM chat WHERE (thread_id = :chatRoomId OR (thread_id IS NULL AND chat_room_id = :chatRoomId)) AND opinion_type = :opinion
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
                WHERE (ch.thread_id = :chatRoomId OR (ch.thread_id IS NULL AND ch.chat_room_id = :chatRoomId)) AND ch.opinion_type = :opinion
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
        WHERE user_id = :userId AND (thread_id = :chatRoomId OR (thread_id IS NULL AND chat_room_id = :chatRoomId))
    ) ch
    JOIN chat_reaction ch_r ON ch.chat_id = ch_r.chat_id
    GROUP BY ch.chat_id, ch.content
    """, nativeQuery = true)
	List<Object[]> findChatHighlight(@Param("userId") Long userId, @Param("chatRoomId") Long chatRoomId);

}
