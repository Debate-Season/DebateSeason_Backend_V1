package com.debateseason_backend_v1.domain.user.service.request;

import com.debateseason_backend_v1.domain.user.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.user.enums.GenderType;

import lombok.Builder;

@Builder
public record ProfileRegisterServiceRequest(
	Long userId,
	String profileColor,
	String nickname,
	Long communityId,
	GenderType gender,
	AgeRangeType ageRange
) {
}