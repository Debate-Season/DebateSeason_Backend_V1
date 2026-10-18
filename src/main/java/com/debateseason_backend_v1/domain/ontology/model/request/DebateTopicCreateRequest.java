package com.debateseason_backend_v1.domain.ontology.model.request;

import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "토론 주제 신설 — 이슈 컨테이너 아래 스레드로 만든다")
public record DebateTopicCreateRequest(

	@Schema(description = "파이프라인 제안 ID. 재호출 시 처음 결과를 돌려준다", example = "topic-new-20261102-07")
	@NotBlank(message = "proposalId 는 필수입니다.")
	@Size(max = 64, message = "proposalId 는 64자 이하입니다.")
	String proposalId,

	@Schema(description = "토론 주제", example = "의대 증원, 2027학년도부터 해야 하나")
	@NotBlank(message = "title 은 필수입니다.")
	@Size(max = 255, message = "title 은 255자 이하입니다.")
	String title,

	@Schema(description = "해시태그", example = "#의대증원 #전공의")
	@Size(max = 255, message = "content 는 255자 이하입니다.")
	String content,

	@Schema(description = "처음 상태. PREPARING(준비 중, 미노출) 또는 OPEN(진행 중). 비우면 OPEN", example = "PREPARING",
		allowableValues = {"PREPARING", "OPEN"})
	ChatRoomStatus status,

	@Size(max = 255)
	String reason,

	@Size(max = 64)
	String approver
) {
}
