package com.debateseason_backend_v1.domain.ontology.model.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "사건 연결 해제 (사건 추가의 롤백). 사건 행은 남는다")
public record EventUnlinkRequest(

	@NotEmpty(message = "eventIds 는 1건 이상이어야 합니다.")
	@Size(max = 50)
	List<Long> eventIds,

	@Size(max = 255)
	String reason,

	@Size(max = 64)
	String approver
) {
}
