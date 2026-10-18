package com.debateseason_backend_v1.domain.issue.model.response;

import com.debateseason_backend_v1.domain.issue.model.IssueStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이슈 상태 변경 결과")
public record IssueStatusChangeResponse(

	@Schema(description = "이슈 id", example = "6")
	Long issueId,

	@Schema(description = "변경 전 상태", example = "PUBLISHED")
	IssueStatus fromStatus,

	@Schema(description = "변경 후 상태", example = "DORMANT")
	IssueStatus toStatus,

	@Schema(description = "실제로 바뀌었는지. 이미 그 상태였으면 false 이고 이력을 남기지 않는다", example = "true")
	boolean changed
) {
}
