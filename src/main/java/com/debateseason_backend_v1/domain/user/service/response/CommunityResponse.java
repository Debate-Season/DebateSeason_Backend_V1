package com.debateseason_backend_v1.domain.user.service.response;

import com.debateseason_backend_v1.domain.repository.entity.Community;

public record CommunityResponse(
	String name,
	String iconUrl
) {

	public static CommunityResponse from(Community community) {

		return new CommunityResponse(
			community.getName(),
			community.getImageUrl()
		);
	}

}