package com.debateseason_backend_v1.domain.user.application.service.request;

import lombok.Builder;

@Builder
public record LogoutServiceRequest(
	Long userId,
	String refreshToken
) {
}