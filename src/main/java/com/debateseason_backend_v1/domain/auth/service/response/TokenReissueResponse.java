package com.debateseason_backend_v1.domain.auth.service.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "토큰 재발급 응답 DTO", description = "토큰 재발급 응답")
public record TokenReissueResponse(
	@Schema(
		description = "Access Token",
		example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzM2Njc1NDQyLCJleHAiOjE3M..."
	)
	String accessToken,

	@Schema(
		description = "Refresh Token",
		example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzM2Njc1NDQyLCJleHAiOjE3M..."
	)
	String refreshToken
) {
}