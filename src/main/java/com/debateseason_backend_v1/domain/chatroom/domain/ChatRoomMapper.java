package com.debateseason_backend_v1.domain.chatroom.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ChatRoomMapper {
	private final Long chatRoomId;
	private final String chatRoomTitle;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private final LocalDateTime createdAt;

	private final Long agree;
	private final Long disagree;
	private final String outDated;
}
