package com.debateseason_backend_v1.domain.issue.model.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IssueResponse {

	private long issueId;

	private String title;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	private Long countChatRoom;

	private Long bookMarks;

	@Override
	public String toString() {
		return "id: "+issueId+" : 제목"+title+" : 작성일"+createdAt.toString()+" : 채팅방 수"+countChatRoom;
	}
}
