package com.debateseason_backend_v1.domain.auth.controller.docs;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.auth.controller.request.TokenReissueRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth API", description = "인증 관련 API")
public interface AuthControllerV1Docs {

	@Operation(
		summary = "토큰 재발급",
		description = """
			Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다.
			만료되거나 유효하지 않은 Refresh Token인 경우 401 에러가 반환됩니다.
			"""
	)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = TokenReissueRequest.class)
		)
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "토큰 재발급 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ApiResult.class),
				examples = @ExampleObject(
					name = "ReissueSuccess",
					summary = "토큰 재발급 성공 응답",
					value = """
						{
						    "status": 200,
						    "code": "SUCCESS",
						    "message": "토큰 재발급에 성공했습니다.",
						    "data": {
						        "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwia...",
						        "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwia..."
						    }
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "401",
			description = "유효하지 않은 Refresh Token",
			content = @Content(
				mediaType = "application/json",
				examples = @ExampleObject(
					value = """
						{
						    "status": 401,
						    "code": "INVALID_REFRESH_TOKEN",
						    "message": "Refresh Token이 유효하지 않습니다."
						}
						"""
				)
			)
		)
	})
	ApiResult<?> reissueToken(@RequestBody TokenReissueRequest request);
}