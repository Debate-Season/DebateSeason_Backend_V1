package com.debateseason_backend_v1.domain.wiki.service.generator;

/**
 * 토론위키 본문 생성기 (v1.4.0 Phase 2) — LLM 호출을 감추는 seam.
 *
 * <p>이 인터페이스 덕분에 {@code WikiGenerationService} 는 특정 LLM 벤더에 묶이지 않는다.
 * Phase 2 골격 단계에서는 유일한 구현이 {@link StubWikiContentGenerator}(미구성 예외)이고,
 * {@code ANTHROPIC_API_KEY} 확보 후 실제 Claude 호출 구현체를 추가해 이 스텁을 대체한다.
 *
 * <p>실 구현체 추가 시:
 * <ol>
 *   <li>{@code build.gradle} 에 {@code com.anthropic:anthropic-java} 추가</li>
 *   <li>{@code AnthropicWikiContentGenerator implements WikiContentGenerator} 작성
 *       ({@link WikiPrompts#SYSTEM_PROMPT} + {@link WikiPrompts#buildUserPrompt} 사용,
 *       streaming + adaptive thinking + effort=high)</li>
 *   <li>실 구현체에 {@code @Primary} 를 붙이거나 {@link StubWikiContentGenerator} 를 제거해
 *       빈 충돌을 피한다</li>
 * </ol>
 */
public interface WikiContentGenerator {

	/**
	 * 원천 재료로 위키 본문(Markdown)을 생성한다. 게시 여부·저장은 호출자 책임(항상 DRAFT 로만 저장).
	 *
	 * @throws com.debateseason_backend_v1.common.exception.CustomException 생성기가 미구성이거나 호출에 실패한 경우
	 */
	GeneratedWiki generate(WikiGenerationContext context);
}
