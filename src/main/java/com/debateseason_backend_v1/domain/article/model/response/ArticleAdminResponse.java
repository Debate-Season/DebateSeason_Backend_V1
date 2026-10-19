package com.debateseason_backend_v1.domain.article.model.response;

import java.time.LocalDateTime;
import java.util.List;

import com.debateseason_backend_v1.domain.article.entity.ArticleStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/** 관리 API 응답 모음. */
public final class ArticleAdminResponse {

	private ArticleAdminResponse() {
	}

	@Schema(description = "적재 결과")
	public record Upsert(Long articleId, ArticleStatus status,
		@Schema(description = "새로 만들었거나 내용이 바뀌었으면 true") boolean changed) {
	}

	@Schema(description = "상태 변경 결과")
	public record StatusChange(Long articleId, ArticleStatus fromStatus, ArticleStatus toStatus, boolean changed,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime datePublished) {
	}

	@Schema(description = "연결 상태")
	public record Links(Long articleId, List<Long> newsEventIds, List<Long> issueIds) {
	}

	@Schema(description = "키워드 교체 결과")
	public record Keywords(Long articleId, List<String> keywords,
		@Schema(description = "같은 목록이면 false") boolean changed) {
	}

	@Schema(description = "확인용 조회")
	public record Summary(Long articleId, String articleKey, String edition, ArticleStatus status, String headline,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime datePublished,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime updatedAt,
		List<Long> newsEventIds, List<Long> issueIds, int sourceCount) {
	}
}
