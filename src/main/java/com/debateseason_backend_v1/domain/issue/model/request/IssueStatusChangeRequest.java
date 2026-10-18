package com.debateseason_backend_v1.domain.issue.model.request;

import com.debateseason_backend_v1.domain.issue.model.IssueStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "이슈 상태 변경 요청 (PUBLISHED·DORMANT·ARCHIVED)")
public record IssueStatusChangeRequest(

	@Schema(description = "바꿀 상태", example = "DORMANT")
	@NotNull(message = "status 는 필수입니다.")
	IssueStatus status,

	@Schema(description = "변경 사유", example = "14일간 새 사건 없음")
	@Size(max = 255, message = "reason 은 255자 이하입니다.")
	String reason,

	@Schema(description = "승인자 (Discord 사용자 ID)", example = "123456789012345678")
	@Size(max = 64, message = "approver 는 64자 이하입니다.")
	String approver
) {
}
