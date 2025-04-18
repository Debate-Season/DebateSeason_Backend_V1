package com.debateseason_backend_v1.domain.profile.service.request;

import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;

import lombok.Builder;

@Builder
public record ProfileUpdateServiceRequest(
	Long userId,
	String profileColor,
	String nickname,
	Long communityId,
	GenderType gender,
	AgeRangeType ageRange
) {
}
