package com.debateseason_backend_v1.domain.article.model.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/** 웹 공개 조회 응답 (익명). 내부 근거(문단 sources·출처 media)는 내지 않는다 — 출처 노출은 법률 검토(D7) 뒤. */
public final class ArticlePublicResponse {

	private ArticlePublicResponse() {
	}

	@Schema(description = "토론철 아티클 단건")
	public record Detail(
		@Schema(description = "아티클 ID (웹 URL 은 이 값의 소문자 base36)", example = "12") Long articleId,
		@Schema(example = "코스피, 8,000선 돌파 뒤 하루 만에 4% 급락") String headline,
		@Schema(description = "평문 2~3문장. 사건 타임라인이 그대로 인용한다") String summary,
		@Schema(description = "본문 문단 (평문)") List<String> body,
		@Schema(example = "토론철 AI 기자") String byline,
		@Schema(description = "AI 생성 여부 (항상 true)", example = "true") boolean aiGenerated,
		@Schema(example = "경제") String articleSection,
		List<String> keywords,
		@Schema(description = "처음 게시된 시각 (Asia/Seoul)")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime datePublished,
		@Schema(description = "마지막 수정 시각 (Asia/Seoul)")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime updatedAt,
		@Schema(description = "관련 사건 (병합된 사건은 흡수한 사건으로)") List<Event> events,
		@Schema(description = "관련 이슈 — 사건 경유와 직접 연결의 합집합, 노출 중인 이슈만") List<Long> issueIds
	) {
	}

	@Schema(description = "아티클의 관련 사건")
	public record Event(
		Long eventId,
		String name,
		@Schema(description = "발생일, 모르면 null", example = "2026-10-16", nullable = true)
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate date,
		@Schema(description = "이 사건이 붙은 노출 중인 이슈") List<Long> issueIds
	) {
	}

	@Schema(description = "이슈 사건 타임라인 페이지")
	public record Timeline(
		@Schema(description = "발생일 내림차순 (날짜 없는 사건은 뒤)") List<TimelineEvent> items,
		@Schema(description = "다음 페이지 cursor. 마지막이면 null", nullable = true) String nextCursor
	) {
	}

	@Schema(description = "타임라인의 사건 1개")
	public record TimelineEvent(
		Long eventId,
		String name,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate date,
		@Schema(description = "이 사건의 게시 아티클, 최신 발행순") List<ArticleCard> articles
	) {
	}

	@Schema(description = "아티클 카드")
	public record ArticleCard(
		Long articleId,
		String headline,
		String summary,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime datePublished
	) {
	}
}
