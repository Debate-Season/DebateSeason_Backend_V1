package com.debateseason_backend_v1.domain.user.domain;

public record UserRegisterCommand(
	String socialId,
	SocialType socialType
) {
}
