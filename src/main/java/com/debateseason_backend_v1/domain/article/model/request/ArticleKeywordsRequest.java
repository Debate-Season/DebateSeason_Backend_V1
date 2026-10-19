package com.debateseason_backend_v1.domain.article.model.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "아티클 연관 키워드 교체 (게시된 기사 포함). 본문 등 다른 필드는 바꾸지 않는다")
public record ArticleKeywordsRequest(

	@Schema(description = "새 키워드 목록 (통째로 교체). 장소 키워드를 앞에", example = "[\"부산\", \"동구\", \"부캉이\"]")
	@NotNull @Size(max = 50)
	List<@Size(max = 64) String> keywords,

	@Size(max = 255)
	String reason,

	@Size(max = 64)
	String approver
) {
}
