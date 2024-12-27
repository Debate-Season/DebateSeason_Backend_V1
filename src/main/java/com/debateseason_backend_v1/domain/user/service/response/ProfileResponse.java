package com.debateseason_backend_v1.domain.user.service.response;

import com.debateseason_backend_v1.domain.repository.entity.Community;
import com.debateseason_backend_v1.domain.repository.entity.Profile;
import com.debateseason_backend_v1.domain.user.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.user.enums.GenderType;

public record ProfileResponse(
	String nickname,
	GenderType gender,
	AgeRangeType ageRange,
	CommunityResponse community
) {

	public static ProfileResponse of(Profile profile, Community community) {

		return new ProfileResponse(
			profile.getNickname(),
			profile.getGender(),
			profile.getAgeRange(),
			CommunityResponse.from(community)
		);
	}

}