package com.debateseason_backend_v1.domain.auth.service.response;

import com.debateseason_backend_v1.common.enums.SocialType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

	private String accessToken;
	private String refreshToken;
	private SocialType socialType;
	private boolean isRegistered;

}