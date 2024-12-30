package com.debateseason_backend_v1.domain.user.controller.request;

import com.debateseason_backend_v1.domain.user.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.user.enums.GenderType;
import com.debateseason_backend_v1.domain.user.service.request.ProfileUpdateServiceRequest;

public record ProfileUpdateRequest(
	String nickname,
	Long communityId,
	GenderType gender,
	AgeRangeType ageRange
) {

	public ProfileUpdateServiceRequest toServiceRequest(Long userId) {

		return ProfileUpdateServiceRequest.builder()
			.userId(userId)
			.nickname(nickname)
			.communityId(communityId)
			.gender(gender)
			.ageRange(ageRange)
			.build();
	}

}