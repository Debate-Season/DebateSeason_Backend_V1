package com.debateseason_backend_v1.domain.user.service.request;

import com.debateseason_backend_v1.domain.user.enums.SocialType;

import lombok.Builder;

@Builder
public record OidcLoginServiceRequest(
	SocialType socialType,
	String idToken
) {
}
