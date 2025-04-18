package com.debateseason_backend_v1.domain.issue;

import java.util.List;

import com.debateseason_backend_v1.domain.issue.model.response.IssueBriefResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationDTO {
	private List<IssueBriefResponse> items;
}
