package com.debateseason_backend_v1.domain.user.controller.docs;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.response.ErrorResponse;
import com.debateseason_backend_v1.common.response.VoidApiResult;
import com.debateseason_backend_v1.domain.user.controller.request.LogoutRequest;
import com.debateseason_backend_v1.domain.user.controller.request.SocialLoginRequest;
import com.debateseason_backend_v1.domain.user.service.response.LoginResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "User API", description = "로그인 관련 API")
public interface UserControllerV1Docs {

	@Operation(
		summary = "로그인",
		description = """
			소셜 타입과 식별자를 이용하여 로그인을 수행합니다. \n
			첫 로그인(프로필 미등록)인 경우 profileStatus가 false, \n
			프로필 등록 후 로그인의 경우 profileStatus가 true로 응답됩니다.
			"""
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = SocialLoginRequest.class)
		)
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "로그인 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(
					oneOf = {
						ApiResult.class,
						LoginResponse.class
					},
					description = "로그인 응답 데이터"
				),
				examples = {
					@ExampleObject(
						name = "FirstLogin",
						summary = "첫 로그인 응답",
						value = """
							{
							    "status": 200,
							    "code": "SUCCESS",
							    "message": "로그인을 성공했습니다.",
							    "data": {
							        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
							        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
							        "socialType": "kakao",
							        "profileStatus": false
							    }
							}
							"""
					),
					@ExampleObject(
						name = "RegisteredLogin",
						summary = "프로필 등록 후 로그인 응답",
						value = """
							{
							    "status": 200,
							    "code": "SUCCESS",
							    "message": "로그인을 성공했습니다.",
							    "data": {
							        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
							        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
							        "socialType": "kakao",
							        "profileStatus": true
							    }
							}
							"""
					)
				}
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "지원하지 않는 소셜 요청",
			content = @Content(
				mediaType = "application/json",
				examples = {
					@ExampleObject(
						name = "MissingSingleRequiredValue",
						summary = "필수 입력값에 빈 문자열 입력",
						value = """
							{
							    "status": 400,
							    "code": "MISSING_REQUIRED_VALUE",
							    "message": "소셜 고유 ID는 필수입니다."
							}
							"""
					),
					@ExampleObject(
						name = "NotSupportedSocialType",
						summary = "지원하지 않는 소셜 타입",
						value = """
							{
							    "status": 400,
							    "code": "NOT_SUPPORTED_SOCIAL_TYPE",
							    "message": "지원하지 않는 소셜 타입입니다"
							}
							"""
					)
				}
			)
		)
	})
	public ApiResult<LoginResponse> login(@RequestBody SocialLoginRequest request);

	@Operation(
		summary = "로그아웃",
		description = """
			✅ API를 사용하려면 Access Token이 필요합니다. \n
			API를 호출 시 Refresh Token을 무효화됩니다. \n
			프론트엔드 단에서는 Access Token과 Refresh Token을 모두 삭제해야 합니다.
			"""
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = LogoutRequest.class)
		)
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "로그아웃 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = VoidApiResult.class),
				examples = @ExampleObject(
					value = """
						{
						    "status": 200,
						    "code": "SUCCESS",
						    "message": "로그아웃을 성공했습니다."
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "잘못된 요청",
			content = @Content(
				mediaType = "application/json",
				examples = {
					@ExampleObject(
						name = "MissingSingleRequiredValue",
						summary = "필수 입력값에 빈 문자열 입력",
						value = """
							{
							    "status": 400,
							    "code": "MISSING_REQUIRED_VALUE",
							    "message": "Refresh Token은 필수입니다."
							}
							"""
					)
				}
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증 실패",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(
					oneOf = {
						ErrorResponse.class
					}
				),
				examples = {
					@ExampleObject(
						name = "ExpiredAccessToken",
						summary = "만료된 Access Token",
						value = """
							{
							    "status": 401,
							    "code": "EXPIRED_ACCESS_TOKEN",
							    "message": "Access Token이 만료되었습니다. 다시 로그인해주세요."
							}
							"""
					),
					@ExampleObject(
						name = "ExpiredRefreshToken",
						summary = "만료된 Refresh Token",
						value = """
							{
							    "status": 401,
							    "code": "EXPIRED_REFRESH_TOKEN",
							    "message": "Refresh Token이 만료되었습니다. 다시 로그인해주세요."
							}
							"""
					),
					@ExampleObject(
						name = "InvalidAccessToken",
						summary = "유효하지 않은 Access Token",
						value = """
							{
							    "status": 401,
							    "code": "INVALID_ACCESS_TOKEN",
							    "message": "유효하지 않은 Access Token 입니다."
							}
							"""
					),
					@ExampleObject(
						name = "InvalidRefreshToken",
						summary = "유효하지 않은 Refresh Token",
						value = """
							{
							    "status": 401,
							    "code": "INVALID_REFRESH_TOKEN",
							    "message": "유효하지 않은 Refresh Token 입니다."
							}
							"""
					)
				}
			)
		)
	})
	VoidApiResult logout(@Valid @RequestBody LogoutRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails);

	@Operation(
		summary = "회원 탈퇴",
		description = """
			✅ API를 사용하려면 Access Token이 필요합니다. \n
			회원 탈퇴를 수행합니다. \n
			탈퇴 시 사용자는 soft delate 되며, 5일 후 사용자의 개인정보가 익명화 처리가 진행됩니다. \n
			탈퇴 후에는 Refresh Token이 무효화됩니다. \n
			프론트엔드 단에서는 Access Token과 Refresh Token을 모두 삭제해야 합니다.
			"""
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "회원 탈퇴 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = VoidApiResult.class),
				examples = @ExampleObject(
					value = """
						{
						    "status": 200,
						    "code": "SUCCESS",
						    "message": "회원 탈퇴가 완료되었습니다."
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "인증 실패",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(
					oneOf = {
						ErrorResponse.class
					}
				),
				examples = {
					@ExampleObject(
						name = "ExpiredAccessToken",
						summary = "만료된 Access Token",
						value = """
							{
							    "status": 401,
							    "code": "EXPIRED_ACCESS_TOKEN",
							    "message": "Access Token이 만료되었습니다. 다시 로그인해주세요."
							}
							"""
					),
					@ExampleObject(
						name = "InvalidAccessToken",
						summary = "유효하지 않은 Access Token",
						value = """
							{
							    "status": 401,
							    "code": "INVALID_ACCESS_TOKEN",
							    "message": "유효하지 않은 Access Token 입니다."
							}
							"""
					)
				}
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "존재하지 않는 회원",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						    "status": 404,
						    "code": "USER_NOT_FOUND",
						    "message": "사용자를 찾을 수 없습니다."
						}
						"""
				)
			)
		)
	})
	VoidApiResult withdraw(@AuthenticationPrincipal CustomUserDetails userDetails);
}
