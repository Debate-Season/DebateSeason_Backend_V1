package com.debateseason_backend_v1.domain.user.controller.request;

import com.debateseason_backend_v1.domain.user.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.user.enums.GenderType;
import com.debateseason_backend_v1.domain.user.service.request.ProfileRegisterServiceRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileRegisterRequest(
	@NotBlank(message = "닉네임은 필수입니다")
	String nickname,

	@NotNull(message = "커뮤니티 선택은 필수입니다")
	Long communityId,

	@NotNull(message = "성별 선택은 필수입니다")
	GenderType gender,

	@NotNull(message = "나이대 선택은 필수입니다")
	AgeRangeType ageRange
) {

	public ProfileRegisterServiceRequest toServiceRequest(Long userId) {

		return ProfileRegisterServiceRequest.builder()
			.userId(userId)
			.nickname(nickname)
			.communityId(communityId)
			.gender(gender)
			.ageRange(ageRange)
			.build();
	}

}