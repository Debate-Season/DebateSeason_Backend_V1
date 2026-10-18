package com.debateseason_backend_v1.domain.article.entity;

/**
 * 토론철 발행 아티클 상태. 데이터 레포 PRD v0.11.0 의 상태 집합을 그대로 받는다.
 * 공개 API 는 PUBLISHED 만 낸다. 관리 API 로 열린 전이는 {@code ArticleAdminService} 참고.
 */
public enum ArticleStatus {
	REVIEW,     // 적재됨, 미노출
	APPROVED,   // 저장만 받는다 (전이 미개방)
	PUBLISHED,  // 공개
	HIDDEN,     // 회수 (미노출, 삭제 아님)
	REJECTED,   // 저장만 받는다
	EXCLUDED    // 저장만 받는다
}
