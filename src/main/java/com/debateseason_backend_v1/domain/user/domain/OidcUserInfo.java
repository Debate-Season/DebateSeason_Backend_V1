package com.debateseason_backend_v1.domain.user.domain;

import com.debateseason_backend_v1.domain.user.enums.SocialType;

public record OidcUserInfo(
	SocialType socialType,
	String identifier
) {
}
