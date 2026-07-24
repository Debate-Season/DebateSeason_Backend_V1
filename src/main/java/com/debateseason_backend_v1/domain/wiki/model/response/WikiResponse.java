package com.debateseason_backend_v1.domain.wiki.model.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * v1.4.0 — 이슈의 게시된 토론위키 응답.
 *
 * 게시본이 없으면 {@code status=null}, {@code content=null} 로 내려간다(에러 아님) — 프론트는 "위키 없음"을 표시.
 */
@Getter
@Builder
@Schema(description = "이슈의 토론위키 (게시본)")
public class WikiResponse {

	@Schema(description = "이슈 id", example = "6")
	private Long issueId;

	@Schema(description = "이슈 제목")
	private String issueTitle;

	@Schema(description = "게시 상태. 게시본이 없으면 null.", example = "PUBLISHED")
	private String status;

	@Schema(description = "본문 (Markdown). 미게시면 null.")
	private String content;

	@Schema(description = "생성 모델명. AI 생성본일 때만.", example = "claude-opus-4-8")
	private String model;

	@Schema(description = "게시 중인 리비전 id. 미게시면 null.", example = "12")
	private Long revisionId;

	@Schema(description = "위키 갱신 시각. 미게시면 null.")
	private LocalDateTime updatedAt;
}
