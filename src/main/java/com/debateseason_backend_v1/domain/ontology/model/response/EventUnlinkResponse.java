package com.debateseason_backend_v1.domain.ontology.model.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사건 연결 해제 결과")
public record EventUnlinkResponse(

	@Schema(description = "실제로 연결이 끊긴 사건 id. 이미 끊겨 있던 것은 빠진다")
	List<Long> unlinkedEventIds
) {
}
