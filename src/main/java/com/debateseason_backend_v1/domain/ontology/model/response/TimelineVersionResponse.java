package com.debateseason_backend_v1.domain.ontology.model.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이슈 타임라인 버전 1개")
public record TimelineVersionResponse(

	@Schema(description = "버전 (1부터)", example = "3")
	int version,

	@Schema(description = "이 버전을 만든 변경 이력 id", example = "42")
	Long changeLogId,

	@Schema(description = "그 시점에 연결된 사건 목록 [{id, name, date}], 날짜순 (날짜 없는 사건은 뒤)")
	JsonNode snapshot,

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime createdAt
) {
}
