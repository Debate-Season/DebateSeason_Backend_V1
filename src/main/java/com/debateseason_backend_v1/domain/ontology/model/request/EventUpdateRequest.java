package com.debateseason_backend_v1.domain.ontology.model.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "사건 수정 — 보낸 필드만 바꾼다. 되돌리기는 POST /api/v1/admin/change-log/{changeLogId}/revert")
public record EventUpdateRequest(

	@Schema(description = "파이프라인 제안 ID. 재호출 시 처음 결과를 돌려준다", example = "event-edit-20261102-02")
	@NotBlank(message = "proposalId 는 필수입니다.")
	@Size(max = 64, message = "proposalId 는 64자 이하입니다.")
	String proposalId,

	@Schema(description = "새 사건명", example = "복지부, 2027학년도 의대 정원 2천 명 증원 확정")
	@Size(max = 255)
	String name,

	@Schema(description = "대분류", example = "사회")
	@Size(max = 64)
	String major,

	@Schema(description = "중분류", example = "의료")
	@Size(max = 64)
	String middle,

	@Schema(description = "발생일. occurredAt 이 있으면 그 날짜로 맞춘다", example = "2026-10-17")
	LocalDate date,

	@Schema(description = "최초 발생 시각 (KST, ISO)", example = "2026-10-17T21:30:00")
	LocalDateTime occurredAt,

	@Schema(description = "추가할 근거 기사 media.id. 이미 붙은 것은 무시한다")
	@Size(max = 100)
	List<Long> addMediaIds,

	@Size(max = 255)
	String reason,

	@Size(max = 64)
	String approver
) {
}
