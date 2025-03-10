package com.debateseason_backend_v1.domain.issue.model.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IssueBriefResponse {

	@Schema(description = "이슈방 Id", example = "1")
	private long issueId;

	@Schema(description = "이슈방 제목", example = "트럼프 관세 정책")
	private String title;

	@Schema(description = "생성일", example = "2024-12-03T08:51:57")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	@Schema(description = "관련 토론방 수", example = "15")
	private Long countChatRoom;

	@Schema(description = "즐겨찾기 수", example = "20")
	private Long bookMarks;

	@Override
	public String toString() {
		return "id: "+issueId+" : 제목"+title+" : 작성일"+createdAt.toString()+" : 채팅방 수"+countChatRoom;
	}
}
