package com.debateseason_backend_v1.domain.ontology.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "변경 되돌리기 (사건 수정·병합의 롤백)")
public record RevertRequest(

	@Size(max = 255)
	String reason,

	@Size(max = 64)
	String approver
) {
}
