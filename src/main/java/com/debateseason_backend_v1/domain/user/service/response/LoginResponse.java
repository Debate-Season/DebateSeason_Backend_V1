package com.debateseason_backend_v1.domain.user.service.response;

import com.debateseason_backend_v1.common.enums.SocialType;

import lombok.Builder;

@Builder
public record LoginResponse(
	String accessToken,
	String refreshToken,
	SocialType socialType,
	Boolean isRegistered
) {
}