package com.debateseason_backend_v1.domain.user.controller.request;

import com.debateseason_backend_v1.domain.user.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.user.enums.GenderType;
import com.debateseason_backend_v1.domain.user.service.request.ProfileUpdateServiceRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "프로필 수정 요청 DTO")
public record ProfileUpdateRequest(
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
	@NotNull(message = "나이대 선택은 필수입니다.")
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