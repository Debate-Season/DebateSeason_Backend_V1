package com.debateseason_backend_v1.domain.profile.presetaion.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.response.VoidApiResult;
import com.debateseason_backend_v1.domain.profile.application.service.ProfileServiceV1;
import com.debateseason_backend_v1.domain.profile.application.service.response.ProfileResponse;
import com.debateseason_backend_v1.domain.profile.presetaion.controller.docs.ProfileControllerV1Docs;
import com.debateseason_backend_v1.domain.profile.presetaion.controller.request.ProfileCreateRequest;
import com.debateseason_backend_v1.domain.profile.presetaion.controller.request.ProfileImageRegisterRequest;
import com.debateseason_backend_v1.domain.profile.presetaion.controller.request.ProfileUpdateRequest;
import com.debateseason_backend_v1.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profiles")
public class ProfileControllerV1 implements ProfileControllerV1Docs {

	private final ProfileServiceV1 profileService;

	@PostMapping
	public VoidApiResult registerProfile(
		@Valid @RequestBody ProfileCreateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		profileService.create(request.toServiceRequest(userDetails.getUserId()));

		return VoidApiResult.success("프로필 등록이 완료되었습니다.");
	}

	@PatchMapping("/image")
	public VoidApiResult registerProfileImage(
		@Valid @RequestBody ProfileImageRegisterRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		profileService.createProfileImage(request.toServiceRequest(userDetails.getUserId()));

		return VoidApiResult.success("프로필 이미지 등록이 완료되었습니다.");
	}

	@GetMapping("/me")
	public ApiResult<ProfileResponse> getMyProfile(
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		ProfileResponse response = profileService.getProfileByUserId(userDetails.getUserId());

		return ApiResult.success("프로필 조회가 완료되었습니다.", response);
	}

	@PatchMapping
	public VoidApiResult updateProfile(
		@Valid @RequestBody ProfileUpdateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		profileService.update(request.toServiceRequest(userDetails.getUserId()));

		return VoidApiResult.success("프로필 수정이 완료되었습니다.");
	}

	@GetMapping("/nickname/check")
	public VoidApiResult checkNicknameDuplicate(
		@RequestParam String query
	) {
		profileService.checkNicknameAvailability(query);

		return VoidApiResult.success("사용 가능한 닉네임입니다.");
	}

}


