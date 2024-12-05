package com.debateseason_backend_v1.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

	// 이거 쓰려면 validation 의존성을 추가해야만 한다.
	@NotBlank
	private String username;

	@NotBlank
	private String password;

	private String role;

	// 소속 커뮤니티
	private String community;

}
