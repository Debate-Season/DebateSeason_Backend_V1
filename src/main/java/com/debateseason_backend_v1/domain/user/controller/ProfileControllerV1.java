package com.debateseason_backend_v1.domain.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.user.controller.request.ProfileRegisterRequest;
import com.debateseason_backend_v1.domain.user.controller.request.ProfileUpdateRequest;
import com.debateseason_backend_v1.domain.user.service.ProfileServiceV1;
import com.debateseason_backend_v1.domain.user.service.response.ProfileResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profiles")
public class ProfileControllerV1 {

	private final ProfileServiceV1 profileService;

	@PostMapping
	public ApiResult<Void> registerProfile(
		@RequestBody @Valid ProfileRegisterRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {

		profileService.register(request.toServiceRequest(userDetails.getUserId()));

		return ApiResult.success("프로필 등록이 완료되었습니다.");
	}

	@GetMapping("/me")
	public ApiResult<ProfileResponse> getMyProfile(
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {

		ProfileResponse response = profileService.getMyProfile(userDetails.getUserId());

		return ApiResult.success("프로필 조회가 완료되었습니다.", response);
	}

	@PatchMapping
	public ApiResult<Void> updateProfile(
		@RequestBody @Valid ProfileUpdateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {

		profileService.update(request.toServiceRequest(userDetails.getUserId()));

		return ApiResult.success("프로필 수정이 완료되었습니다.");
	}

	// public ApiResult<?> checkDuplicateNickname(){
	//
	// 	profileService
	//
	// 	return ApiResult("")
	// }

}


