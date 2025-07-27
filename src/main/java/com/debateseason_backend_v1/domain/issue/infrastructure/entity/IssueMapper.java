package com.debateseason_backend_v1.domain.issue.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 혹시나 Mapper를 다시 setter하는 실수를 하지 않기 위해서!
@Getter
@Builder
@AllArgsConstructor
public class IssueMapper {

	private final Long id;
	private final String title; // 이슈 제목
	private final Long bookMarks; // 북마크 개수
}
