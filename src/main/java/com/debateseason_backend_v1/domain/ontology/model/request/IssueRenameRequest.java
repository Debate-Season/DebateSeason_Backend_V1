package com.debateseason_backend_v1.domain.ontology.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이슈명 변경. 롤백은 응답의 previousTitle 로 같은 API 를 다시 부른다")
public record IssueRenameRequest(

	@Schema(description = "새 이슈명", example = "의대 정원 확대와 전공의 이탈")
	@NotBlank(message = "title 은 필수입니다.")
	@Size(max = 255, message = "title 은 255자 이하입니다.")
	String title,

	@Size(max = 255)
	String reason,

	@Size(max = 64)
	String approver
) {
}
