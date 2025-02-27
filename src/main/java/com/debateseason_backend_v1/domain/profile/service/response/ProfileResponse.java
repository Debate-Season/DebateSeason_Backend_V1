package com.debateseason_backend_v1.domain.profile.service.response;

import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.repository.entity.Profile;
import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 프로필 조회 응답")
public record ProfileResponse(
	// @Schema(description = "프로필 컬러", example = "RED")
	// String profileColor,

	@Schema(description = "닉네임", example = "토론왕")
	String nickname,

	@Schema(description = "성별", example = "남성")
	GenderType gender,

	@Schema(description = "연령대", example = "20대")
	AgeRangeType ageRange,

	@Schema(description = "프로필에 등록된 커뮤니티 응답")
	CommunityResponse community
) {

	public static ProfileResponse of(Profile profile, CommunityType communityType) {

		return new ProfileResponse(
			// profile.getProfileColor(),
			profile.getNickname(),
			profile.getGender(),
			profile.getAgeRange(),
			CommunityResponse.from(communityType)
		);
	}

}