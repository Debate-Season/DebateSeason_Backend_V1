package com.debateseason_backend_v1.domain.user.controller.docs;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.response.VoidApiResult;
import com.debateseason_backend_v1.common.swagger.ApiErrorCode;
import com.debateseason_backend_v1.domain.user.controller.request.LogoutRequest;
import com.debateseason_backend_v1.domain.user.controller.request.SocialLoginRequest;
import com.debateseason_backend_v1.domain.user.service.response.LoginResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "User API", description = "계정 관리 API")
public interface UserControllerV1Docs {

	@Operation(
		summary = "로그인",
		description = """
			사용자를 로그인 시킵니다. \n
			첫 로그인(프로필 미등록)인 경우 `profileStatus`가 `false`, \n
			프로필 등록 후 로그인의 경우 `profileStatus`가 `true`로 응답됩니다.
			"""
	)
	@ApiResponse(responseCode = "200", description = "로그인 성공")
	@ApiErrorCode({
		ErrorCode.MISSING_REQUIRED_SOCIAL_ID,
		ErrorCode.MISSING_REQUIRED_SOCIAL_TYPE,
		ErrorCode.NOT_SUPPORTED_SOCIAL_TYPE,
	})
	public ApiResult<LoginResponse> login(@RequestBody SocialLoginRequest request);

	@Operation(
		summary = "로그아웃",
		description = """
			사용자를 로그아웃 시킵니다. \n
			❗️클라이언트에서도 `Access Token`과 `Refresh Token`을 모두 삭제해야 합니다.❗
			"""
	)
	@ApiResponse(responseCode = "200", description = "로그아웃 성공")
	@ApiErrorCode({
		ErrorCode.EXPIRED_ACCESS_TOKEN,
		ErrorCode.EXPIRED_REFRESH_TOKEN,
		ErrorCode.INVALID_ACCESS_TOKEN,
		ErrorCode.INVALID_REFRESH_TOKEN,
		ErrorCode.MISSING_REFRESH_TOKEN,
	})
	VoidApiResult logout(
		@Valid @RequestBody LogoutRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	);

	@Operation(
		summary = "회원 탈퇴",
		description = """
			회원 탈퇴를 수행합니다. \n
			❗️클라이언트에서도 `Access Token`과 `Refresh Token`을 모두 삭제해야 합니다.❗
			"""
	)
	@ApiResponse(responseCode = "200", description = "회원 탈퇴 성공")
	@ApiErrorCode({
		ErrorCode.MISSING_REFRESH_TOKEN,
		ErrorCode.EXPIRED_ACCESS_TOKEN,
		ErrorCode.INVALID_ACCESS_TOKEN,
		ErrorCode.NOT_FOUND_USER
	})
	VoidApiResult withdraw(@AuthenticationPrincipal CustomUserDetails userDetails);
}
