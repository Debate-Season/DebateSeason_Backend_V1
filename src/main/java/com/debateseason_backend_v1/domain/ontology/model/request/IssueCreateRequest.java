package com.debateseason_backend_v1.domain.ontology.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이슈 신설 (온톨로지 승인 반영)")
public record IssueCreateRequest(

	@Schema(description = "파이프라인 제안 ID. 같은 값으로 다시 부르면 새로 만들지 않고 처음 결과를 돌려준다", example = "issue-new-20261102-03")
	@NotBlank(message = "proposalId 는 필수입니다.")
	@Size(max = 64, message = "proposalId 는 64자 이하입니다.")
	String proposalId,

	@Schema(description = "이슈명", example = "의대 정원 확대")
	@NotBlank(message = "title 은 필수입니다.")
	@Size(max = 255, message = "title 은 255자 이하입니다.")
	String title,

	@Schema(description = "대분류", example = "사회")
	@NotBlank(message = "majorCategory 는 필수입니다.")
	@Size(max = 255)
	String majorCategory,

	@Schema(description = "중분류", example = "의료")
	@Size(max = 255)
	String middleCategory,

	@Schema(description = "사유", example = "신규 사건 5건이 기존 이슈와 연결되지 않음")
	@Size(max = 255)
	String reason,

	@Schema(description = "승인자 (Discord 사용자 ID)", example = "123456789012345678")
	@Size(max = 64)
	String approver
) {
}
