package com.debateseason_backend_v1.domain.wiki.service.generator;

/**
 * LLM 생성 결과 (v1.4.0 Phase 2).
 *
 * @param content Markdown 본문
 * @param model   생성에 쓴 모델명 (리비전의 {@code model} 에 기록). 예: {@code claude-opus-4-8}
 */
public record GeneratedWiki(
	String content,
	String model
) {
}
