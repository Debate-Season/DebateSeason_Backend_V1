package com.debateseason_backend_v1.domain.chatroom.model.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomResponse {
	@Schema(description = "토론방 ID", example = "1")
	private Long chatRoomId;

	@Schema(description = "토론방 제목", example = "윤석열은 탄핵되어야 한다")
	private String title;

	@Schema(description = "내용", example = "#정부 #윤석열")
	private String content;

	@Schema(description = "생성일", example = "2025-03-03T06:09:04.324Z")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	@Schema(description = "투표", example = "AGREE")
	private String opinion;

	@Schema(description = "찬성", example = "10")
	private Long agree;

	@Schema(description = "반대", example = "11")
	private Long disagree;
}
