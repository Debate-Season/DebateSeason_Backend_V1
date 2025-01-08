package com.debateseason_backend_v1.domain.chatroom.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatRoomDAO {

	private long chatRoomId;

	//private Issue issue;

	private String title;
	private String content;

	// 상태 메시지는 UPPERCASE로 해서, 통일시킴
	private int agree;

	private int disagree;

	// CreateDate 반환
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime createdAt;

	private String opinion;
}
