package com.debateseason_backend_v1.domain.issue.model.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IssueResponse {

	private String title;

	private LocalDate createDate;

	private Long countChatRoom;
}
