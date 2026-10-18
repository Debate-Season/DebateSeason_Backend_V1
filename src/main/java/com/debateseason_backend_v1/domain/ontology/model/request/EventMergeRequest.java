package com.debateseason_backend_v1.domain.ontology.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "사건 병합(타임라인 개정) — drop 을 keep 에 흡수한다. 되돌리기는 change-log revert")
public record EventMergeRequest(

	@Schema(description = "파이프라인 제안 ID. 재호출 시 처음 결과를 돌려준다", example = "event-merge-20261102-01")
	@NotBlank(message = "proposalId 는 필수입니다.")
	@Size(max = 64, message = "proposalId 는 64자 이하입니다.")
	String proposalId,

	@Schema(description = "남길 사건 id", example = "12")
	@NotNull(message = "keepEventId 는 필수입니다.")
	Long keepEventId,

	@Schema(description = "흡수될 사건 id. 이 이슈와의 연결이 끊기고 merged_into 가 남는다 (행은 보존)", example = "15")
	@NotNull(message = "dropEventId 는 필수입니다.")
	Long dropEventId,

	@Size(max = 255)
	String reason,

	@Size(max = 64)
	String approver
) {
}
