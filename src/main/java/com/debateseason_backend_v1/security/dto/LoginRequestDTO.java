package com.debateseason_backend_v1.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청")
public record LoginRequestDTO(
	@Schema(description = "사용자 이름", example = "user123", required = true)
	String username,

	@Schema(description = "비밀번호", example = "password123", required = true)
	String password
) {
}
