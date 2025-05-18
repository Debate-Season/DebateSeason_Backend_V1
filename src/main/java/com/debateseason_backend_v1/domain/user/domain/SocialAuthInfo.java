package com.debateseason_backend_v1.domain.user.domain;

public record SocialAuthInfo(
	String socialId,
	SocialType socialType
) {
}
