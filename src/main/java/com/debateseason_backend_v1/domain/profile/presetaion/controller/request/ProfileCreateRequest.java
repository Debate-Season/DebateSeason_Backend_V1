package com.debateseason_backend_v1.domain.profile.presetaion.controller.request;

import com.debateseason_backend_v1.domain.profile.application.service.request.ProfileCreateServiceRequest;
import com.debateseason_backend_v1.domain.profile.domain.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.domain.DistrictType;
import com.debateseason_backend_v1.domain.profile.domain.GenderType;
import com.debateseason_backend_v1.domain.profile.domain.ProvinceType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(title = "프로필 등록 요청 DTO", description = "프로필 등록 요청")
public record ProfileCreateRequest(

	@Schema(description = "사용자 닉네임", example = "토론왕")
	@NotBlank(message = "닉네임은 필수입니다.")
	String nickname,

	@Schema(description = "소속 커뮤니티 ID", example = "1")
	@NotNull(message = "커뮤니티 선택은 필수입니다.")
	Long communityId,

	@Schema(description = "성별", example = "남성")
	@NotNull(message = "성별 선택은 필수입니다.")
	GenderType gender,

	@Schema(description = "연령대", example = "20대")
	@NotNull(message = "연령대 선택은 필수입니다.")
	AgeRangeType ageRange,

	@Schema(description = "거주 시도 코드", example = "11")
	ProvinceType residenceProvince,

	@Schema(description = "거주 시·군·구 코드", example = "11030")
	DistrictType residenceDistrict,

	@Schema(description = "출신 시도 코드", example = "21")
	ProvinceType hometownProvince,

	@Schema(description = "출신 시·군·구 코드", example = "21010")
	DistrictType hometownDistrict
) {

	public ProfileCreateServiceRequest toServiceRequest(Long userId) {

		return ProfileCreateServiceRequest.builder()
			.userId(userId)
			.nickname(nickname)
			.communityId(communityId)
			.gender(gender)
			.ageRange(ageRange)
			.residenceProvince(residenceProvince)
			.residenceDistrict(residenceDistrict)
			.hometownProvince(hometownProvince)
			.hometownDistrict(hometownDistrict)
			.build();
	}

}