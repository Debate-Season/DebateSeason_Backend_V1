package com.debateseason_backend_v1.domain.wiki.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 밖에서 생성한 AI 초안을 DRAFT 리비전으로 올린다 (로컬 claude -p 생성 등).
 * 서버 안 생성({@code /generate})과 같은 결과(source=AI)를 남기되, LLM 호출은 서버 밖에서 한다.
 */
@Schema(description = "AI 위키 초안 업로드 (DRAFT, 게시 안 함)")
public record WikiDraftUploadRequest(

	@Schema(description = "이슈 id", example = "6")
	@NotNull(message = "issueId 는 필수입니다.")
	Long issueId,

	@Schema(description = "본문 (Markdown)")
	@NotBlank(message = "content 는 필수입니다.")
	@Size(max = 200_000, message = "content 가 너무 깁니다.")
	String content,

	@Schema(description = "본문 요약. 2~3문장 평문, 150자 안팎", example = "…")
	@Size(max = 500, message = "summary 는 500자 이하입니다.")
	String summary,

	@Schema(description = "생성 모델 id", example = "claude-opus-5-5")
	@NotBlank(message = "model 은 필수입니다.")
	@Size(max = 64)
	String model,

	@Schema(description = "리비전 메모 (생성 조건 등)", example = "로컬 claude -p, 뉴스 제목 120건·본문 8건 근거")
	@Size(max = 255)
	String editSummary
) {
}
