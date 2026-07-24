package com.debateseason_backend_v1.domain.wiki.domain;

/**
 * 위키 리비전의 생성 주체 (v1.4.0 AI 토론위키).
 *
 * <ul>
 *   <li>{@link #AI}    — LLM(Claude) 이 생성한 리비전. `model` 에 모델명이 남는다.</li>
 *   <li>{@link #ADMIN} — ADMIN 이 수동 편집한 리비전. `created_by` 에 user_id 가 남는다.</li>
 * </ul>
 */
public enum WikiSource {

	AI,
	ADMIN
}
