package com.debateseason_backend_v1.domain.user.domain;

import com.debateseason_backend_v1.domain.user.enums.UserStatus;

public record UserPersistenceData(
	UserId id,
	SocialAuthInfo socialAuthInfo,
	UserStatus status
) {
}
