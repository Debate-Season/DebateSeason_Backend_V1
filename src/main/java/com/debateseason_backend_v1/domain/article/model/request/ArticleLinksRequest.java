package com.debateseason_backend_v1.domain.article.model.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "연결 교체. 보낸 목록만 통째로 바꾸고, null 인 목록은 그대로 둔다")
public record ArticleLinksRequest(
	@Size(max = 50) List<Long> newsEventIds,
	@Size(max = 50) List<Long> issueIds,
	@Size(max = 64) String approver
) {
}
