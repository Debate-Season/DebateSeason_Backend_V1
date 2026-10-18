package com.debateseason_backend_v1.domain.sitemap;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.issue.model.IssueVisibility;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * 사이트맵 피드 쿼리. 노출 규칙은 앱 목록과 같다:
 * <ul>
 *   <li>이슈: {@link IssueVisibility} — PUBLISHED·DORMANT 이고 위키 게시본이 있는 것</li>
 *   <li>토론 주제: 스레드(레거시 NULL 포함), OPEN·CLOSED, 소속 이슈가 노출 대상</li>
 * </ul>
 * 이 규칙이 목록 쿼리와 어긋나면 사이트맵이 앱에 없는 페이지를 내보내게 된다. 함께 고친다.
 */
@Repository
@RequiredArgsConstructor
class SitemapRepository {

	private static final String VISIBLE_ISSUE = IssueVisibility.LISTED_I;
	private static final String VISIBLE_THREAD =
		"(r.room_type IS NULL OR r.room_type = 'THREAD') AND r.status IN ('OPEN','CLOSED')";

	private final EntityManager em;

	/**
	 * GREATEST 는 인자 하나만 NULL 이어도 NULL 이다. 각 항을 created_at 으로 메워서 넘긴다.
	 * 위키는 게시 중(PUBLISHED)일 때만 센다 — 초안 저장은 페이지를 바꾸지 않는다.
	 */
	@SuppressWarnings("unchecked")
	List<Object[]> issues(long afterId, int limit) {
		return em.createNativeQuery("""
				SELECT i.issue_id,
				       GREATEST(
				           COALESCE(i.updated_at, i.created_at),
				           COALESCE(MAX(r.created_at), i.created_at),
				           COALESCE(MAX(CASE WHEN w.status = 'PUBLISHED' THEN w.updated_at END), i.created_at)
				       ) AS updated_at
				FROM issue i
				LEFT JOIN chat_room r ON r.issue_id = i.issue_id AND %s
				LEFT JOIN debate_wiki w ON w.issue_id = i.issue_id
				WHERE %s
				  AND i.issue_id > :afterId
				GROUP BY i.issue_id, i.updated_at, i.created_at
				ORDER BY i.issue_id
				LIMIT :limit
				""".formatted(VISIBLE_THREAD, VISIBLE_ISSUE))
			.setParameter("afterId", afterId)
			.setParameter("limit", limit)
			.getResultList();
	}

	@SuppressWarnings("unchecked")
	List<Object[]> threads(long afterId, int limit) {
		return em.createNativeQuery("""
				SELECT r.chat_room_id, r.created_at
				FROM chat_room r
				INNER JOIN issue i ON i.issue_id = r.issue_id
				WHERE %s AND %s
				  AND r.chat_room_id > :afterId
				ORDER BY r.chat_room_id
				LIMIT :limit
				""".formatted(VISIBLE_THREAD, VISIBLE_ISSUE))
			.setParameter("afterId", afterId)
			.setParameter("limit", limit)
			.getResultList();
	}

	/** 게시된 토론철판 아티클. 수정 시각 = max(updated_at, date_published). */
	@SuppressWarnings("unchecked")
	List<Object[]> articles(long afterId, int limit) {
		return em.createNativeQuery("""
				SELECT a.id, GREATEST(COALESCE(a.updated_at, a.date_published), a.date_published)
				FROM toronchul_article a
				WHERE a.status = 'PUBLISHED' AND a.edition = 'toronchul'
				  AND a.id > :afterId
				ORDER BY a.id
				LIMIT :limit
				""")
			.setParameter("afterId", afterId)
			.setParameter("limit", limit)
			.getResultList();
	}

	/**
	 * 스레드별 마지막 채팅 시각. 이관 후 메시지는 thread_id 로, 이관 전 레거시 메시지는 chat_room_id 로 방을 가리킨다
	 * (랭킹 쿼리와 같은 COALESCE 규칙). JOIN/LEAVE 는 활동으로 치지 않는다.
	 *
	 * <p>두 조건을 OR 로 묶으면 MariaDB 가 인덱스를 못 쓰고 chat 전체를 훑는다(EXPLAIN type=ALL).
	 * thread_id·chat_room_id 각각의 인덱스를 타도록 UNION ALL 로 나눈다.
	 */
	@SuppressWarnings("unchecked")
	Map<Long, LocalDateTime> lastChatAt(List<Long> threadIds) {
		Map<Long, LocalDateTime> result = new HashMap<>();
		if (threadIds.isEmpty()) {
			return result;
		}
		List<Object[]> rows = em.createNativeQuery("""
				SELECT x.rid, MAX(x.ts)
				FROM (
				    SELECT c.thread_id AS rid, c.time_stamp AS ts
				    FROM chat c
				    WHERE c.thread_id IN (:ids) AND c.message_type = 'CHAT'
				    UNION ALL
				    SELECT c.chat_room_id, c.time_stamp
				    FROM chat c
				    WHERE c.thread_id IS NULL AND c.chat_room_id IN (:ids) AND c.message_type = 'CHAT'
				) x
				GROUP BY x.rid
				""")
			.setParameter("ids", threadIds)
			.getResultList();
		for (Object[] row : rows) {
			result.put(((Number)row[0]).longValue(), toLocalDateTime(row[1]));
		}
		return result;
	}

	static LocalDateTime toLocalDateTime(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof LocalDateTime ldt) {
			return ldt;
		}
		if (value instanceof Timestamp ts) {
			return ts.toLocalDateTime();
		}
		return LocalDateTime.parse(value.toString().replace(' ', 'T'));
	}
}
