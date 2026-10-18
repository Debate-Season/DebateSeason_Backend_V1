package com.debateseason_backend_v1.domain.article.model.request;

import com.debateseason_backend_v1.domain.article.entity.ArticleStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "아티클 상태 변경. 허용: REVIEW→PUBLISHED, REVIEW→HIDDEN, PUBLISHED→HIDDEN, HIDDEN→PUBLISHED")
public record ArticleStatusChangeRequest(
	@NotNull ArticleStatus status,
	@Size(max = 255) String reason,
	@Size(max = 64) String approver
) {
}
