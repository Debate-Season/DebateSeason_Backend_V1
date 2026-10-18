package com.debateseason_backend_v1.domain.article.model.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "아티클 적재. articleKey 가 새것이면 REVIEW 로 만들고, 있으면 한 번도 게시되지 않았을 때만 내용을 갱신한다")
public record ArticleUpsertRequest(

	@Schema(description = "멱등 키", example = "2026-10-18/toronchul/20261018-019")
	@NotBlank @Size(max = 80)
	String articleKey,

	@Schema(description = "판", example = "toronchul", allowableValues = {"toronchul", "tmoney"})
	@NotBlank @Pattern(regexp = "toronchul|tmoney", message = "edition 은 toronchul 또는 tmoney 입니다.")
	String edition,

	@NotBlank @Size(max = 255)
	String headline,

	@Schema(description = "평문 2~3문장")
	@Size(max = 2000)
	String summary,

	@Schema(description = "문단 배열. sources 는 내부 근거(media.id)로 공개 응답에 나가지 않는다")
	@NotEmpty @Size(max = 200)
	List<@Valid Paragraph> body,

	@Schema(example = "경제") @Size(max = 32)
	String articleSection,

	@Size(max = 50)
	List<@Size(max = 64) String> keywords,

	@Size(max = 255)
	String debatePoint,

	@Schema(description = "비우면 '토론철 AI 기자'") @Size(max = 32)
	String byline,

	@Schema(description = "비우면 true")
	Boolean aiGenerated,

	BigDecimal sensitivityMax,

	Map<String, Object> sensitivityAxes,

	@Size(max = 80)
	String generatorVersion,

	@Schema(description = "출처 media.id. 하나라도 없으면 404 + 무저장")
	@Size(max = 200)
	List<Long> sourceMediaIds,

	@Schema(description = "직접 연결 이슈 (보조). 공개 API 는 사건 경유와 합집합으로 낸다")
	@Size(max = 50)
	List<Long> issueIds,

	@Size(max = 50)
	List<Long> newsEventIds,

	@Schema(description = "데이터 쪽 생성 시각. 비우면 적재 시각")
	LocalDateTime createdAt,

	@Size(max = 64)
	String approver
) {

	public record Paragraph(
		@NotBlank @Size(max = 10_000) String text,
		@Size(max = 50) List<Long> sources
	) {
	}
}
