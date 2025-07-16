package com.debateseason_backend_v1.domain.issue.model.response;

import java.util.List;

import com.debateseason_backend_v1.domain.issue.mapper.IssueRoomBriefMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class IssueBriefContainer {
	private List<IssueRoomBriefMapper> items;
}
