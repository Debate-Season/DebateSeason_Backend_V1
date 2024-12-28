package com.debateseason_backend_v1.domain.user.service.response;

import com.debateseason_backend_v1.domain.repository.entity.Community;

public record ProfileCommunityResponse(
	String name,
	String iconUrl
) {

	public static ProfileCommunityResponse from(Community community) {

		return new ProfileCommunityResponse(
			community.getName(),
			community.getIconUrl()
		);
	}

}