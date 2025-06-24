package com.debateseason_backend_v1.domain.user.application.service.request;

import com.debateseason_backend_v1.domain.user.domain.SocialType;

import lombok.Builder;

@Builder
public record SocialLoginServiceRequest(
	String identifier,
	SocialType socialType
) {
}