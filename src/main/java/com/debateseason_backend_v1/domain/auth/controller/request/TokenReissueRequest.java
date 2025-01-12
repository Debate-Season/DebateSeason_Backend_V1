package com.debateseason_backend_v1.domain.auth.controller.request;

import com.debateseason_backend_v1.domain.auth.service.request.TokenReissueServiceRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청")
public record TokenReissueRequest(
	@Schema(
		description = "Refresh Token",
		example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiw..."
	)

	@NotBlank(message = "Refresh Token은 필수 값입니다.")
	String refreshToken
) {
	public TokenReissueServiceRequest toServiceRequest() {
		return TokenReissueServiceRequest.builder()
			.refreshToken(refreshToken)
			.build();
	}
}
