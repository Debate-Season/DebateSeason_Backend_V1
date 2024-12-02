package com.debateseason_backend_v1.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RegisterDTO {

	@NotBlank
	private final String username;

	@NotBlank
	private final String password;

	@NotBlank
	private final String role;

	private String community;
}