package com.debateseason_backend_v1.domain.article.model.request;

import java.util.List;

import com.debateseason_backend_v1.domain.article.model.request.ArticleUpsertRequest.Paragraph;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "아티클 수정 (게시된 기사 포함). 보낸 필드만 바꾸고, 수정 전 내용은 리비전으로 남는다")
public record ArticleEditRequest(

	@Schema(description = "파이프라인 제안 ID. 재호출 시 처음 결과", example = "article-edit-20261019-14")
	@NotBlank @Size(max = 64)
	String proposalId,

	@Size(max = 255)
	String headline,

	@Size(max = 2000)
	String summary,

	@Schema(description = "본문 문단 배열 (보내면 통째로 교체)")
	@Size(max = 200)
	List<@Valid Paragraph> body,

	@Schema(description = "연관 키워드 (보내면 통째로 교체, 앞뒤 공백·중복 정리). 장소 키워드를 앞에")
	@Size(max = 50)
	List<@Size(max = 64) String> keywords,

	@Size(max = 32)
	String articleSection,

	@Size(max = 255)
	String reason,

	@Size(max = 64)
	String approver
) {
}
