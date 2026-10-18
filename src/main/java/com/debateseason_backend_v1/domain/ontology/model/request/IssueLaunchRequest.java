package com.debateseason_backend_v1.domain.ontology.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "이슈 공개 (위키 게시 + 이슈 PUBLISHED 를 한 번에)")
public record IssueLaunchRequest(

	@Schema(description = "파이프라인 제안 ID. 재호출 시 처음 결과", example = "issue-launch-20261020-03")
	@NotBlank @Size(max = 64)
	String proposalId,

	@Schema(description = "게시할 위키 리비전 (이 이슈의 DRAFT 리비전)", example = "35")
	@NotNull
	Long wikiRevisionId,

	@Schema(description = "승인자 (Discord 사용자 ID)") @Size(max = 64)
	String approver,

	@Size(max = 255)
	String reason
) {
}
