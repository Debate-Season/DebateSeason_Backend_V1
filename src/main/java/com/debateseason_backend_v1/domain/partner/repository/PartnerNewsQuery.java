package com.debateseason_backend_v1.domain.partner.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * 고객사 뉴스 조회. 대상은 늘 같은 조건이다: 해당 판(edition) + PUBLISHED.
 * 최신순 커서는 (date_published, id) 내림차순 — 같은 시각에 여러 편이 발행돼도 순서가 확정된다.
 */
@Repository
@RequiredArgsConstructor
public class PartnerNewsQuery {

	private final EntityManager em;

	/** 키워드 정확 일치 + 최신순. cursorAt 이 null 이면 첫 페이지. */
	@SuppressWarnings("unchecked")
	public List<Long> byKeyword(String edition, String keyword, LocalDateTime cursorAt, Long cursorId, int limit) {
		return toIds(em.createNativeQuery("""
				SELECT a.id
				FROM toronchul_article_keyword k
				JOIN toronchul_article a ON a.id = k.article_id
				WHERE k.keyword = :keyword AND a.edition = :edition AND a.status = 'PUBLISHED'
				  AND (:cursorAt IS NULL OR a.date_published < :cursorAt OR (a.date_published = :cursorAt AND a.id < :cursorId))
				ORDER BY a.date_published DESC, a.id DESC
				LIMIT :limit
				""")
			.setParameter("keyword", keyword)
			.setParameter("edition", edition)
			.setParameter("cursorAt", cursorAt)
			.setParameter("cursorId", cursorId == null ? 0L : cursorId)
			.setParameter("limit", limit)
			.getResultList());
	}

	/** 키워드 없이 최신순. */
	@SuppressWarnings("unchecked")
	public List<Long> latest(String edition, LocalDateTime cursorAt, Long cursorId, int limit) {
		return toIds(em.createNativeQuery("""
				SELECT a.id
				FROM toronchul_article a
				WHERE a.edition = :edition AND a.status = 'PUBLISHED'
				  AND (:cursorAt IS NULL OR a.date_published < :cursorAt OR (a.date_published = :cursorAt AND a.id < :cursorId))
				ORDER BY a.date_published DESC, a.id DESC
				LIMIT :limit
				""")
			.setParameter("edition", edition)
			.setParameter("cursorAt", cursorAt)
			.setParameter("cursorId", cursorId == null ? 0L : cursorId)
			.setParameter("limit", limit)
			.getResultList());
	}

	/** 인기 뉴스 후보: since 이후 발행분 전부 (하루 수십 편이라 7일치도 수백 건). */
	@SuppressWarnings("unchecked")
	public List<Long> publishedSince(String edition, LocalDateTime since) {
		return toIds(em.createNativeQuery("""
				SELECT a.id FROM toronchul_article a
				WHERE a.edition = :edition AND a.status = 'PUBLISHED' AND a.date_published >= :since
				""")
			.setParameter("edition", edition)
			.setParameter("since", since)
			.getResultList());
	}

	private static List<Long> toIds(List<Object> rows) {
		return rows.stream().map(r -> ((Number)r).longValue()).toList();
	}
}
