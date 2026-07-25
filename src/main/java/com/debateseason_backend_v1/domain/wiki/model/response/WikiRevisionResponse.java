package com.debateseason_backend_v1.domain.wiki.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 리비전 생성 결과 (v1.4.0 Phase 2) — AI 생성/수동 편집 공통.
 *
 * 방금 만든 리비전은 항상 DRAFT 로만 저장된다(게시는 별도 {@code /publish}). 본문 전체 대신 미리보기만 담는다.
 */
@Getter
@Builder
@Schema(description = "토론위키 리비전 생성 결과")
public class WikiRevisionResponse {

	@Schema(description = "위키 id", example = "3")
	private Long wikiId;

	@Schema(description = "생성된 리비전 id", example = "12")
	private Long revisionId;

	@Schema(description = "이슈 id", example = "6")
	private Long issueId;

	@Schema(description = "위키 게시 상태 (리비전 추가는 게시 상태를 바꾸지 않음)", example = "DRAFT")
	private String status;

	@Schema(description = "리비전 생성 주체", example = "AI")
	private String source;

	@Schema(description = "생성 모델명 (AI 리비전만)", example = "claude-opus-4-8")
	private String model;

	@Schema(description = "본문 미리보기 (앞부분)")
	private String preview;
}
