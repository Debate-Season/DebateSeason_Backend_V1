package com.debateseason_backend_v1.domain.issue.model;

/**
 * 이슈가 목록·홈 랭킹·사이트맵에 나오는 조건 (네이티브 쿼리 조각). 사용자 결정 2026-10-19:
 * 상태가 PUBLISHED·DORMANT 이고 <b>토론위키 게시본이 있는</b> 이슈만 앱·웹 목록에 나온다.
 * 위키 없이 빈 페이지가 노출·색인되던 문제(신설 이슈) 때문이다. 위키를 내리면 이슈도 목록에서 빠진다.
 *
 * <p>이슈 상세(GET /api/v1/issue)는 이 조건을 쓰지 않는다 — ADMIN·데이터 작업과 직접 링크용으로 열어 둔다.
 * 쿼리마다 따로 쓰면 어긋나므로 여기 하나만 고친다. 앞뒤 공백은 텍스트 블록에 이어 붙일 때를 위한 것이다.
 */
public final class IssueVisibility {

	private IssueVisibility() {
	}

	/** FROM issue (별칭 없음) */
	public static final String LISTED =
		" (issue.status IN ('PUBLISHED','DORMANT') AND EXISTS (SELECT 1 FROM debate_wiki w WHERE w.issue_id = issue.issue_id"
			+ " AND w.status = 'PUBLISHED' AND w.published_revision_id IS NOT NULL)) ";

	/** 별칭 iss */
	public static final String LISTED_ISS =
		" (iss.status IN ('PUBLISHED','DORMANT') AND EXISTS (SELECT 1 FROM debate_wiki w WHERE w.issue_id = iss.issue_id"
			+ " AND w.status = 'PUBLISHED' AND w.published_revision_id IS NOT NULL)) ";

	/** 별칭 i */
	public static final String LISTED_I =
		" (i.status IN ('PUBLISHED','DORMANT') AND EXISTS (SELECT 1 FROM debate_wiki w WHERE w.issue_id = i.issue_id"
			+ " AND w.status = 'PUBLISHED' AND w.published_revision_id IS NOT NULL)) ";
}
