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

	@Schema(description = "수정 결과")
	public record Edit(Long articleId,
		@Schema(description = "바뀐 게 없거나 같은 proposalId 재호출이면 false") boolean changed,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime updatedAt,
		@Schema(description = "수정 전 내용을 남긴 리비전 id. 되돌리기는 changeLogId 로", nullable = true) Long revisionId,
		@Schema(description = "변경 이력 id. POST /admin/change-log/{id}/revert 로 되돌린다", nullable = true) Long changeLogId) {
	}

	@Schema(description = "확인용 조회")
	public record Summary(Long articleId, String articleKey, String edition, ArticleStatus status, String headline,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime datePublished,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
		LocalDateTime updatedAt,
		List<Long> newsEventIds, List<Long> issueIds, int sourceCount,
		@Schema(description = "연관 키워드 (색인과 같은 값)") List<String> keywords) {
	}
}
