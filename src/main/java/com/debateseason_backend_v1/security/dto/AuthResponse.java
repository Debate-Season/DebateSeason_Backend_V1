package com.debateseason_backend_v1.security.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
	private int status;
	private String message;
	private String username;
	private String role;
}