package com.debateseason_backend_v1.domain.profile.service.request;

import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.DistrictType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;
import com.debateseason_backend_v1.domain.profile.enums.ProvinceType;

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