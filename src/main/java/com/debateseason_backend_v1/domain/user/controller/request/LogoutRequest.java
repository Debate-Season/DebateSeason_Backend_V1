package com.debateseason_backend_v1.domain.user.controller.request;

import com.debateseason_backend_v1.domain.user.service.request.LogoutServiceRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(title = "로그아웃 요청 DTO", description = "로그아웃 요청")
public record LogoutRequest(
	@Schema(description = "리프레시 토큰",
		example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
		format = "jwt"
	)
	@NotBlank(message = "Refresh Token은 필수입니다.")
	String refreshToken
) {

	public LogoutServiceRequest toServiceRequest(Long userId) {

		return LogoutServiceRequest.builder()
			.refreshToken(refreshToken)
			.userId(userId)
			.build();
	}

}
