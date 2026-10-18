package com.debateseason_backend_v1.domain.partner.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/** 고객사 뉴스 응답. 티머니 필수 7항목(제목·본문·요약·작성일시·카테고리·연관 키워드·조회 수) + AI 생성 표시. */
public final class PartnerNewsResponse {

	private PartnerNewsResponse() {
	}

	// 티머니 요구사항: 키워드 목록 응답도 공급 항목(제목·본문·요약·작성일시·카테고리·연관 키워드·조회 수)을 모두 담는다.
	@Schema(name = "PartnerNewsItem", description = "뉴스 목록 항목 (필수 7항목 모두 포함)")
	public record Item(
		@Schema(description = "뉴스 ID", example = "15") Long newsId,
		@Schema(description = "기사 제목", example = "서울 지하철 2호선 강남역 출구 공사, 11월 말까지") String title,
		@Schema(description = "기사 본문 문단") List<String> body,
		@Schema(description = "기사 요약문 (1~2문장)") String summary,
		@Schema(description = "기사 카테고리", example = "사회") String category,
		@Schema(description = "기사 연관 키워드. 장소 키워드가 앞에 온다") List<String> keywords,
		@Schema(description = "기사 작성일시 (Asia/Seoul)", example = "2026-10-19T09:00:00")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime publishedAt,
		@Schema(description = "기사 조회 수 (이 고객사 기준)", example = "1204") long viewCount,
		@Schema(description = "기사 이미지 URL. 현재 제공하지 않음", nullable = true) String imageUrl,
		@Schema(description = "AI 생성 기사 표시 (AI기본법 제31조). 항상 true", example = "true") boolean aiGenerated
	) {
	}

	@Schema(name = "PartnerNewsDetail", description = "뉴스 상세")
	public record Detail(
		Long newsId,
		String title,
		String summary,
		@Schema(description = "기사 본문 문단") List<String> body,
		String category,
		List<String> keywords,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime publishedAt,
		long viewCount,
		@Schema(nullable = true) String imageUrl,
		@Schema(description = "AI 생성 기사 표시. 항상 true") boolean aiGenerated
	) {
	}

	@Schema(name = "PartnerNewsPage", description = "뉴스 목록 페이지")
	public record Page(
		List<Item> items,
		@Schema(description = "다음 페이지 cursor. 마지막이면 null", nullable = true) String nextCursor
	) {
	}
}
