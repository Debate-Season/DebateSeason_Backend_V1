package com.debateseason_backend_v1.domain.profile.application.service.request;

import com.debateseason_backend_v1.domain.profile.domain.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.domain.DistrictType;
import com.debateseason_backend_v1.domain.profile.domain.GenderType;
import com.debateseason_backend_v1.domain.profile.domain.ProvinceType;

import lombok.Builder;

@Builder
public record ProfileRegisterServiceRequest(
	Long userId,
	String profileImage,
	String nickname,
	Long communityId,
	GenderType gender,
	AgeRangeType ageRange,
	ProvinceType residenceProvince,
	DistrictType residenceDistrict,
	ProvinceType hometownProvince,
	DistrictType hometownDistrict
) {
}