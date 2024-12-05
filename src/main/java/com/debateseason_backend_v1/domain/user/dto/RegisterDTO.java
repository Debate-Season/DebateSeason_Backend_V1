package com.debateseason_backend_v1.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {

	@NotBlank
	private String username;

	@NotBlank
	private String password;

	@NotBlank
	private String role;

	private String community;
}