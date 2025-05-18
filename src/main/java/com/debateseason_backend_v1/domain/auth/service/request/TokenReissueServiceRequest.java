package com.debateseason_backend_v1.domain.auth.service.request;

import com.debateseason_backend_v1.domain.user.domain.UserId;

import lombok.Builder;

@Builder
public record TokenReissueServiceRequest(
	UserId userId,
	String refreshToken
) {
}
