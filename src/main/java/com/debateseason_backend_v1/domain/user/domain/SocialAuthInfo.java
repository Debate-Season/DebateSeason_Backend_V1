package com.debateseason_backend_v1.domain.user.domain;

import com.debateseason_backend_v1.domain.user.enums.SocialType;

public record SocialAuthInfo(
	String socialId,
	SocialType socialType
) {
}
