package com.debateseason_backend_v1.domain.issue.model;

/**
 * 이슈 생명주기.
 *
 * v1.3.2 에서는 컬럼만 도입하고 조회 필터는 걸지 않는다.
 * 지금 필터를 걸면 기존 17건의 기본값에 따라 목록이 갑자기 비어 보일 수 있어서,
 * 노출 규칙은 위키 상태와 함께 v1.3.6 에서 켠다.
 *
 * DORMANT 는 노출 규칙이 아니라 분류다. 목록·홈에서 거르지 않고 응답의 status 로만 알리며,
 * 프론트가 이슈맵에서 "잠자는 이슈" 로 따로 묶는다. 채팅·위키도 PUBLISHED 와 똑같이 동작한다.
 * 데이터 파이프라인(온톨로지)이 "14일간 새 사건 없음" 을 제안하고 사람이 승인하면 전환된다.
 */
public enum IssueStatus {

	DRAFT,      // 작성 중 (미노출)
	PUBLISHED,  // 노출
	DORMANT,    // 잠자는 이슈 (노출 유지, 분류만 다름)
	ARCHIVED;   // 보관 (미노출, 삭제 아님)

	/**
	 * 네이티브 쿼리가 돌려준 status 컬럼 값을 해석한다.
	 * 응답의 status 는 non-null 계약이라, 비어 있으면 엔티티 기본값과 같은 PUBLISHED 로 본다.
	 */
	public static IssueStatus fromColumn(Object value) {
		if (value == null) {
			return PUBLISHED;
		}
		return valueOf(value.toString());
	}
}
