package com.debateseason_backend_v1.domain.issue;

import java.util.List;

import com.debateseason_backend_v1.domain.issue.model.response.IssueBriefResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationDTO {
	// 와일드 카드로 무엇이든 들어가도 된다.
	private List<?> items;
}
