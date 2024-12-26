package com.debateseason_backend_v1.domain.user.service.request;

import com.debateseason_backend_v1.domain.user.enums.SocialType;

public record SocialLoginServiceRequest(
	String externalId,
	SocialType socialType
) {
}