package com.debateseason_backend_v1.domain.wiki.service.generator;

import java.util.List;

/**
 * 토론위키 생성용 원천 재료 (v1.4.0 Phase 2).
 *
 * 앱 DB에서 조립한다 — 이슈 메타 + 그 이슈의 스레드(옛 방) 제목들(=주요 쟁점).
 * LLM 호출({@link WikiContentGenerator})에 넘기는 입력이자, 프롬프트({@link WikiPrompts})의 재료.
 */
public record WikiGenerationContext(
	Long issueId,
	String issueTitle,
	String majorCategory,
	String middleCategory,
	List<String> threadTitles
) {
}
