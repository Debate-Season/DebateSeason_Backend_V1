package com.debateseason_backend_v1.domain.user.controller.docs;

import org.springframework.web.bind.annotation.RequestBody;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.user.controller.request.SocialLoginRequest;
import com.debateseason_backend_v1.domain.user.service.response.LoginResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User API", description = "로그인 관련 API")
public interface UserControllerV1Docs {

	@Operation(
		summary = "소셜 로그인",
		description = """
			소셜 타입과 식별자를 이용하여 로그인을 수행합니다.
			첫 로그인(프로필 미등록)인 경우 profileStatus가 false,
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
				schema = @Schema(implementation = ApiResult.class),
				examples = {
					@ExampleObject(
						name = "FirstLogin",
						summary = "첫 로그인 응답",
						value = """
							{
							    "status": 200,
							    "code": "SUCCESS",
							    "message": "소셜 로그인 성공",
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
							    "message": "소셜 로그인 성공",
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
				examples = @ExampleObject(
					value = """
						{
						    "status": 400,
						    "code": "NOT_SUPPORTED_SOCIAL_TYPE",
						    "message": "지원하지 않는 소셜 타입입니다"
						}
						"""
				)
			)
		)
	})
	public ApiResult<LoginResponse> socialLogin(@RequestBody SocialLoginRequest request);

}
