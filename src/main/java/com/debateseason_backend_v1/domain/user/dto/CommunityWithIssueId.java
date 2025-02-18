package com.debateseason_backend_v1.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityWithIssueId {

	private String community;
	private Long issueId;
}
