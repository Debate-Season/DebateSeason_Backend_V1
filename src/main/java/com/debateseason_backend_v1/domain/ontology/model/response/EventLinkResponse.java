package com.debateseason_backend_v1.domain.ontology.model.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사건 추가 결과")
public record EventLinkResponse(

	@Schema(description = "요청 순서대로의 사건 id. 롤백은 이 목록으로 DELETE")
	List<Long> eventIds,

	@Schema(description = "같은 proposalId 재호출이면 false")
	boolean created
) {
}
