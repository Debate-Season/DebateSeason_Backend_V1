package com.debateseason_backend_v1.domain.ontology.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "변경 결과. 되돌리려면 changeLogId 로 revert 를 부른다")
public record ChangeLogResponse(

	@Schema(description = "변경 이력 id. 바뀐 게 없으면 null", example = "42", nullable = true)
	Long changeLogId,

	@Schema(description = "이번 호출로 바뀌었는지. 같은 proposalId 재호출·바뀔 게 없으면 false")
	boolean changed
) {
}
