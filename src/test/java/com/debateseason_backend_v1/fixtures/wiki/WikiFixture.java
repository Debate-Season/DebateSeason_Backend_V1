package com.debateseason_backend_v1.fixtures.wiki;

import jakarta.persistence.EntityManager;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 이슈가 목록·랭킹·사이트맵에 나오려면 위키 게시본이 있어야 한다 (IssueVisibility).
 * 노출을 기대하는 테스트는 이걸로 게시본을 붙인다. 리비전 본문은 노출 판정과 무관해 만들지 않는다.
 */
public final class WikiFixture {

	private WikiFixture() {
	}

	private static final String INSERT =
		"INSERT INTO debate_wiki (issue_id, status, published_revision_id) VALUES (%d, 'PUBLISHED', 1)";

	public static void publish(JdbcTemplate jdbc, Long issueId) {
		jdbc.update(INSERT.formatted(issueId));
	}

	public static void publish(EntityManager em, Long issueId) {
		em.createNativeQuery(INSERT.formatted(issueId)).executeUpdate();
	}
}
