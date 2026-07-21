package com.debateseason_backend_v1.domain.issue.model;

/**
 * 이슈 생명주기.
 *
 * v1.3.2 에서는 컬럼만 도입하고 조회 필터는 걸지 않는다.
 * 지금 필터를 걸면 기존 17건의 기본값에 따라 목록이 갑자기 비어 보일 수 있어서,
 * 노출 규칙은 위키 상태와 함께 v1.3.6 에서 켠다.
 */
public enum IssueStatus {

	DRAFT,      // 작성 중 (미노출)
	PUBLISHED,  // 노출
	ARCHIVED    // 보관 (미노출, 삭제 아님)
}
