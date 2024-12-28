package com.debateseason_backend_v1.domain.community.service.response;

import com.debateseason_backend_v1.domain.repository.entity.Community;

public record CommunityResponse(
	Long id,
	String name,
	String iconUrl
) {

	public static CommunityResponse from(Community community) {

		return new CommunityResponse(
			community.getId(),
			community.getName(),
			community.getIconUrl()
		);
	}

}