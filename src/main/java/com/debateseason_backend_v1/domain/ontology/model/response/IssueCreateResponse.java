package com.debateseason_backend_v1.domain.ontology.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이슈 신설 결과")
public record IssueCreateResponse(

	@Schema(description = "이슈 id", example = "31")
	Long issueId,

	@Schema(description = "함께 만든 컨테이너 방 id (모바일이 입장하는 방)", example = "120")
	Long containerRoomId,

	@Schema(description = "이번 호출로 만들었는지. 같은 proposalId 재호출이면 false", example = "true")
	boolean created
) {
}
