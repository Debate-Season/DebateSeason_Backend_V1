package com.debateseason_backend_v1.domain.user.service.response;

import com.debateseason_backend_v1.domain.repository.entity.Community;
import com.debateseason_backend_v1.domain.repository.entity.Profile;

public record ProfileResponse(
	Long profileId,
	String nickname,
	CommunityResponse community
) {

	public static ProfileResponse of(Profile profile, Community community) {

		return new ProfileResponse(
			profile.getId(),
			profile.getNickname(),
			CommunityResponse.from(community)
		);
	}

}