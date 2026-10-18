package com.debateseason_backend_v1.domain.sitemap;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/** 사이트맵 피드 응답 모음. 제목 등은 일부러 뺀다 — 피드는 id 와 lastmod 만 필요하다. */
public final class SitemapEntry {

	private SitemapEntry() {
	}

	@Schema(description = "이슈 사이트맵 항목")
	public record Issue(

		@Schema(description = "이슈 ID", example = "26")
		Long issueId,

		@Schema(description = "이슈 페이지 내용이 마지막으로 바뀐 시각 (Asia/Seoul). "
			+ "이슈 생성·수정, 노출 중인 토론 주제 추가, 위키 게시 중 가장 늦은 시각", example = "2026-10-19T01:55:55")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime updatedAt
	) {
	}

	@Schema(description = "토론 주제(스레드) 사이트맵 항목")
	public record Thread(

		@Schema(description = "스레드(토론 주제) ID", example = "57")
		Long threadId,

		@Schema(description = "마지막 채팅 시각 (Asia/Seoul). 채팅이 없으면 스레드 생성 시각", example = "2026-10-18T22:14:03")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime lastActivityAt
	) {
	}

	@Schema(description = "토론철 아티클 사이트맵 항목")
	public record Article(

		@Schema(description = "아티클 ID", example = "12")
		Long articleId,

		@Schema(description = "마지막 수정 시각 (Asia/Seoul). 게시 시각 이후 수정이 없으면 게시 시각", example = "2026-10-19T09:00:00")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime updatedAt
	) {
	}

	@Schema(description = "커서 페이지")
	public record Page<T>(

		@Schema(description = "항목. id 오름차순")
		List<T> items,

		@Schema(description = "다음 페이지의 cursor. 마지막 페이지면 null", example = "26", nullable = true)
		Long nextCursor
	) {
	}
}
