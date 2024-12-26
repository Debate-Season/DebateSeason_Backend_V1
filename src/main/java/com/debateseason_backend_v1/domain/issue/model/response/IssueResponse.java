package com.debateseason_backend_v1.domain.issue.model.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IssueResponse {

	private String title;

	private LocalDateTime createdAt;

	private Long countChatRoom;
}
