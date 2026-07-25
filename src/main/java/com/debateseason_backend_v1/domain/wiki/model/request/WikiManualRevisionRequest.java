package com.debateseason_backend_v1.domain.wiki.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * ADMIN 수동 편집 리비전 추가 요청 (v1.4.0 Phase 2).
 * AI 초안의 사실 오류 수정 등에 사용한다({@code source=ADMIN}).
 */
@Schema(description = "토론위키 수동 편집 리비전 추가 요청")
public record WikiManualRevisionRequest(

	@Schema(description = "이슈 id", example = "6")
	@NotNull(message = "issueId 는 필수입니다.")
	Long issueId,

	@Schema(description = "본문 (Markdown)")
	@NotBlank(message = "content 는 필수입니다.")
	String content,

	@Schema(description = "변경 요약", example = "타임라인 날짜 오류 수정")
	String editSummary
) {
}
