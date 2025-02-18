package com.debateseason_backend_v1.domain.user.service.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "로그인 응답")
@Builder
public record LoginResponse(
	@Schema(description = "액세스 토큰",
		example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0QGdtYWlsLmNvbSI...")
	String accessToken,

	@Schema(description = "리프레시 토큰",
		example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0QGdtYWlsLmNvbSI...")
	String refreshToken,

	@Schema(description = "소셜 로그인 타입", example = "apple")
	String socialType,

	@Schema(description = "프로필 생성 여부", example = "false")
	boolean profileStatus
) {
}