package com.debateseason_backend_v1.domain.profile.presetaion.controller.request;

import com.debateseason_backend_v1.domain.profile.application.service.request.ProfileImageRegisterServiceRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "프로필 이미지 등록 요청 DTO", description = "프로필 이미지 등록 요청")
public record ProfileImageRegisterRequest(
	@Schema(description = "프로필 이미지", example = "RED")
	String profileImage
) {
	public ProfileImageRegisterServiceRequest toServiceRequest(Long userId) {

		return ProfileImageRegisterServiceRequest.builder()
			.userId(userId)
			.profileImage(profileImage)
			.build();
	}
}
