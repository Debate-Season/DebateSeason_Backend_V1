package com.debateseason_backend_v1.domain.issue.model.response;

import java.util.List;

import com.debateseason_backend_v1.domain.issue.mapper.IssueBriefResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IssueBriefContainer {
	private List<IssueBriefResponse> items;
}
