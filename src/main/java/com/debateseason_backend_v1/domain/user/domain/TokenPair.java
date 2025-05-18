package com.debateseason_backend_v1.domain.user.domain;

public record TokenPair(
	String accessToken,
	String refreshToken
) {
}
