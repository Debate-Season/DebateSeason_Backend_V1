package com.debateseason_backend_v1.domain.profile.presetaion.controller.docs;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.response.VoidApiResult;
import com.debateseason_backend_v1.common.swagger.ApiErrorCode;
import com.debateseason_backend_v1.domain.profile.application.service.response.ProfileResponse;
import com.debateseason_backend_v1.domain.profile.presetaion.controller.request.ProfileCreateRequest;
import com.debateseason_backend_v1.domain.profile.presetaion.controller.request.ProfileUpdateRequest;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Profile API", description = "프로필 API")
public interface ProfileControllerV1Docs {

	@Operation(
		summary = "프로필 등록",
		description = "사용자의 프로필을 등록합니다.",
		security = @SecurityRequirement(name = "JWT")
	)
	@ApiResponse(responseCode = "200", description = "프로필 등록 성공")
	@ApiErrorCode({
		ErrorCode.MISSING_ACCESS_TOKEN,
		ErrorCode.EXPIRED_ACCESS_TOKEN,
		ErrorCode.INVALID_ACCESS_TOKEN,
		ErrorCode.MISSING_REQUIRED_NICKNAME,
		ErrorCode.MISSING_REQUIRED_COMMUNITY,
		ErrorCode.MISSING_REQUIRED_GENDER_TYPE,
		ErrorCode.MISSING_REQUIRED_AGE_RANGE,
		ErrorCode.INVALID_NICKNAME_PATTERN,
		ErrorCode.NOT_SUPPORTED_COMMUNITY,
		ErrorCode.NOT_SUPPORTED_GENDER_TYPE,
		ErrorCode.NOT_SUPPORTED_AGE_RANGE,
		ErrorCode.DUPLICATE_NICKNAME,
	})
	public VoidApiResult registerProfile(
		@RequestBody ProfileCreateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	);

	@Operation(
		summary = "내 프로필 조회",
		description = "사용자 자신의 프로필을 조회합니다."
	)
	@ApiResponse(responseCode = "200", description = "프로필 조회 성공")
	@ApiErrorCode({
		ErrorCode.MISSING_ACCESS_TOKEN,
		ErrorCode.EXPIRED_ACCESS_TOKEN,
		ErrorCode.INVALID_ACCESS_TOKEN,
		ErrorCode.NOT_FOUND_PROFILE,
	})
	public ApiResult<ProfileResponse> getMyProfile(
		@AuthenticationPrincipal CustomUserDetails userDetails
	);

	@Operation(
		summary = "프로필 수정",
		description = "사용자의 프로필을 수정합니다."
	)
	@ApiResponse(responseCode = "200", description = "프로필 수정 성공")
	@ApiErrorCode({
		ErrorCode.MISSING_ACCESS_TOKEN,
		ErrorCode.EXPIRED_ACCESS_TOKEN,
		ErrorCode.INVALID_ACCESS_TOKEN,
		ErrorCode.MISSING_REQUIRED_NICKNAME,
		ErrorCode.MISSING_REQUIRED_COMMUNITY,
		ErrorCode.MISSING_REQUIRED_GENDER_TYPE,
		ErrorCode.MISSING_REQUIRED_AGE_RANGE,
		ErrorCode.INVALID_NICKNAME_PATTERN,
		ErrorCode.NOT_SUPPORTED_COMMUNITY,
		ErrorCode.NOT_SUPPORTED_GENDER_TYPE,
		ErrorCode.NOT_SUPPORTED_AGE_RANGE,
		ErrorCode.DUPLICATE_NICKNAME,
		ErrorCode.NOT_FOUND_PROFILE
	})
	public VoidApiResult updateProfile(
		@RequestBody ProfileUpdateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	);

	@Operation(
		summary = "닉네임 검사",
		description = "닉네임이 사용 가능한지 검사합니다."
	)
	@Parameter(
		name = "query",
		description = "검사할 닉네임",
		required = true,
		example = "홍길동",
		schema = @Schema(type = "string")
	)
	@ApiResponse(responseCode = "200", description = "닉네임 사용 가능")
	@ApiErrorCode({
		ErrorCode.MISSING_ACCESS_TOKEN,
		ErrorCode.EXPIRED_ACCESS_TOKEN,
		ErrorCode.INVALID_ACCESS_TOKEN,
		ErrorCode.INVALID_NICKNAME_PATTERN,
		ErrorCode.DUPLICATE_NICKNAME
	})
	public VoidApiResult checkNicknameDuplicate(
		@RequestParam String query
	);

}