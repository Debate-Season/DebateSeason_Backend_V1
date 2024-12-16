package com.debateseason_backend_v1.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterRequest {

	private String imageUrl;
	private String nickname;
	private String community;
	private String gender;
	private String ageRange;

}