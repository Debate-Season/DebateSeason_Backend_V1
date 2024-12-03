package com.debateseason_backend_v1.security.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {
	private String username;
	private String role;
}