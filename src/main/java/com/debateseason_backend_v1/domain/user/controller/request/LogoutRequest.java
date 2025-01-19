package com.debateseason_backend_v1.domain.user.controller.request;

import com.debateseason_backend_v1.domain.user.service.request.LogoutServiceRequest;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
	@NotBlank(message = "리프레시 토큰은 필수입니다")
	String refreshToken
) {

	public LogoutServiceRequest toServiceRequest(Long userId) {

		return LogoutServiceRequest.builder()
			.refreshToken(refreshToken)
			.userId(userId)
			.build();
	}

}
