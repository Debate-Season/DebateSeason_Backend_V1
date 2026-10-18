package com.debateseason_backend_v1.domain.ontology.model.request;

import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "토론 주제(스레드) 상태 변경 — OPEN ↔ HIDDEN. 신설 롤백은 HIDDEN")
public record TopicStatusChangeRequest(

	@Schema(description = "바꿀 상태", example = "HIDDEN")
	@NotNull(message = "status 는 필수입니다.")
	ChatRoomStatus status,

	@Size(max = 255)
	String reason,

	@Size(max = 64)
	String approver
) {
}
