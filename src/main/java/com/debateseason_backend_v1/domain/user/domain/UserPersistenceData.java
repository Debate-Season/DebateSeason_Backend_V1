package com.debateseason_backend_v1.domain.user.domain;

public record UserPersistenceData(
	UserId id,
	SocialAuthInfo socialAuthInfo,
	UserStatus status
) {
}
