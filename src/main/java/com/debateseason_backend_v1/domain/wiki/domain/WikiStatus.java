package com.debateseason_backend_v1.domain.wiki.domain;

/**
 * 토론위키 게시 상태 (v1.4.0 AI 토론위키).
 *
 * <ul>
 *   <li>{@link #DRAFT}     — AI 생성 직후 또는 편집 중. 서빙에 노출되지 않는다.</li>
 *   <li>{@link #PUBLISHED} — ADMIN 검수 후 게시. `GET /api/v2/wiki` 가 이 상태만 반환한다.</li>
 *   <li>{@link #HIDDEN}    — 게시 내렸으나 이력 보존.</li>
 * </ul>
 */
public enum WikiStatus {

	DRAFT,
	PUBLISHED,
	HIDDEN
}
