package com.debateseason_backend_v1.domain.profile.domain;

import lombok.Builder;

@Builder
public record ProfileMappingData(
	Long id,
	Long userId,
	Long communityId,
	String profileImage,
	Nickname nickname,
	GenderType gender,
	AgeRangeType ageRange,
	Region residence,
	Region hometown
) {
}
