package com.debateseason_backend_v1.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponseDTO(
	@Schema(description = "사용자 이름", example = "user123")
	String username,

	@Schema(description = "사용자 권한", example = "ROLE_USER")
	String role
) {
}