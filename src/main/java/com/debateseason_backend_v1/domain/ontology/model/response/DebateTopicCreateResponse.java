package com.debateseason_backend_v1.domain.ontology.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토론 주제 신설 결과")
public record DebateTopicCreateResponse(

	@Schema(description = "스레드(토론 주제) id. 롤백은 PATCH /api/v1/admin/room/{id}/status HIDDEN", example = "121")
	Long chatRoomId,

	@Schema(description = "소속 컨테이너 방 id", example = "120")
	Long containerRoomId,

	@Schema(description = "같은 proposalId 재호출이면 false")
	boolean created
) {
}
