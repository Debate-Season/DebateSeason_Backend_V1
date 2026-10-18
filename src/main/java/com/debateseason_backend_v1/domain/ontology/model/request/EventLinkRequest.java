package com.debateseason_backend_v1.domain.ontology.model.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "사건 추가 — 사건을 만들고 이슈에 연결한다")
public record EventLinkRequest(

	@Schema(description = "파이프라인 제안 ID. 재호출 시 처음 결과를 돌려준다", example = "event-add-20261102-11")
	@NotBlank(message = "proposalId 는 필수입니다.")
	@Size(max = 64, message = "proposalId 는 64자 이하입니다.")
	String proposalId,

	@NotEmpty(message = "events 는 1건 이상이어야 합니다.")
	@Size(max = 50, message = "events 는 한 번에 50건까지입니다.")
	List<@Valid Event> events,

	@Size(max = 255)
	String reason,

	@Size(max = 64)
	String approver
) {

	@Schema(name = "EventLinkItem", description = "사건 1건")
	public record Event(

		@Schema(description = "사건명", example = "복지부, 2027학년도 의대 정원 발표")
		@NotBlank(message = "name 은 필수입니다.")
		@Size(max = 255)
		String name,

		@Schema(description = "발생일. occurredAt 이 있으면 그 날짜로 맞춘다", example = "2026-10-30")
		LocalDate date,

		@Schema(description = "최초 발생 시각 (KST, ISO)", example = "2026-10-17T21:30:00")
		LocalDateTime occurredAt,

		@Schema(description = "대분류", example = "사회")
		@Size(max = 64)
		String major,

		@Schema(description = "중분류", example = "의료")
		@Size(max = 64)
		String middle,

		@Schema(description = "근거 기사 media.id 목록")
		@Size(max = 100)
		List<Long> mediaIds
	) {
	}
}
