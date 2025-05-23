package com.debateseason_backend_v1.domain.chatroom.model.response.chatroom;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@AllArgsConstructor
public class ChatRoomTemplate {
	@Schema(description = "채팅방 ID",example = "1")
	private long chatRoomId;

	@Schema(description = "토론방 제목",example = "동덕여대는 폭력시위이다.")
	private String title;

	@Schema(description = "토론방 내용",example = "#동덕여대 #폭력시원")
	private String content;

	// 상태 메시지는 UPPERCASE로 해서, 통일시킴
	@Schema(description = "찬성 수",example = "15")
	private int agree;

	@Schema(description = "반대 수",example = "10")
	private int disagree;

	// CreateDate 반환
	@Schema(description = "생성일",example = "2024-12-03T08:51:57")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime createdAt;

	@Schema(description = "투표", example = "AGREE")
	private String opinion;
}
