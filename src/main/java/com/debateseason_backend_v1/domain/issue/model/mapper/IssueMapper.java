package com.debateseason_backend_v1.domain.issue.model.mapper;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueMapper {
	private Long id;
	private String title;
	private String majorCategory;
	private String middleCategory;
	private LocalDateTime createdAt;
	private Long bookMarks;
}
