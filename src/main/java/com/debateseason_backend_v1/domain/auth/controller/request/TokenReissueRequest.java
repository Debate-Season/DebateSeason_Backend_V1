package com.debateseason_backend_v1.domain.auth.controller.request;

import com.debateseason_backend_v1.domain.auth.service.request.TokenReissueServiceRequest;
import com.debateseason_backend_v1.domain.user.domain.UserId;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(title = "토큰 재발급 요청 DTO", description = "토큰 재발급 요청")
public record TokenReissueRequest(
	@Schema(
		description = "Refresh Token",
		example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiw..."
	)
	@NotBlank(message = "Refresh Token은 필수입니다.")
	String refreshToken
) {
	public TokenReissueServiceRequest toServiceRequest(Long userId) {
		return TokenReissueServiceRequest.builder()
			.refreshToken(refreshToken)
			.userId(new UserId(userId))
			.build();
	}
}
