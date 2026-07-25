package com.debateseason_backend_v1.domain.wiki.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 토론위키 게시 요청 (v1.4.0 Phase 2). 해당 리비전을 PUBLISHED 로 전환한다.
 */
@Schema(description = "토론위키 게시 요청")
public record WikiPublishRequest(

	@Schema(description = "게시할 리비전 id", example = "12")
	@NotNull(message = "revisionId 는 필수입니다.")
	Long revisionId
) {
}
