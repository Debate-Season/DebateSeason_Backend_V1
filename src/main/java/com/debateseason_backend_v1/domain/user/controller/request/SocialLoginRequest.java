package com.debateseason_backend_v1.domain.user.controller.request;

import com.debateseason_backend_v1.common.enums.SocialType;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
	@NotBlank
	String externalId,

	@NotBlank
	SocialType socialType
) {
}