package com.debateseason_backend_v1.domain.profile.application.service.response;

import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "내 프로필 조회 응답 DTO", description = "내 프로필 조회 응답")
public record ProfileResponse(
	@Schema(description = "프로필 이미지", example = "RED")
	String profileImage,

	@Schema(description = "닉네임", example = "토론왕")
	String nickname,

	@Schema(description = "성별", example = "남성")
	GenderType gender,

	@Schema(description = "연령대", example = "20대")
	AgeRangeType ageRange,

	@Schema(description = "프로필에 등록된 커뮤니티 응답")
	CommunityResponse community,

	@Schema(description = "거주 시도 코드", example = "11")
	String residenceProvince,

	@Schema(description = "거주 시군구 코드", example = "11030")
	String residenceDistrict,

	@Schema(description = "출신 시도 코드", example = "21")
	String hometownProvince,

	@Schema(description = "출신 시군구 코드", example = "21010")
	String hometownDistrict
) {

	public static ProfileResponse of(ProfileEntity profile, CommunityType communityType) {

		return ProfileResponse.builder()
			.profileImage(profile.getProfileImage())
			.nickname(profile.getNickname())
			.gender(profile.getGender())
			.ageRange(profile.getAgeRange())
			.residenceProvince(profile.getResidence().getProvinceType().getCode())
			.residenceDistrict(profile.getResidence().getDistrictType().getCode())
			.hometownProvince(profile.getHometown().getProvinceType().getCode())
			.hometownDistrict(profile.getHometown().getDistrictType().getCode())
			.community(CommunityResponse.from(communityType))
			.build();
	}

}