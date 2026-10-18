package com.debateseason_backend_v1.domain.ontology.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "되돌리기 결과")
public record RevertResponse(

	@Schema(description = "되돌리기 자체의 이력 id", example = "43")
	Long changeLogId,

	@Schema(description = "되돌린 원본 이력 id", example = "42")
	Long revertedChangeLogId,

	@Schema(description = "이미 되돌렸으면 false")
	boolean changed
) {
}
