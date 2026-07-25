package com.debateseason_backend_v1.domain.wiki.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 토론위키 게시 결과 (v1.4.0 Phase 2).
 */
@Getter
@Builder
@Schema(description = "토론위키 게시 결과")
public class WikiPublishResponse {

	@Schema(description = "위키 id", example = "3")
	private Long wikiId;

	@Schema(description = "이슈 id", example = "6")
	private Long issueId;

	@Schema(description = "게시 상태", example = "PUBLISHED")
	private String status;

	@Schema(description = "게시 중인 리비전 id", example = "12")
	private Long publishedRevisionId;
}
