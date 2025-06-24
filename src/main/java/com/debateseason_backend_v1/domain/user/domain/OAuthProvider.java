package com.debateseason_backend_v1.domain.user.domain;

import java.util.Arrays;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OAuthProvider {

	KAKAO("kakao", "https://kauth.kakao.com", "https://kauth.kakao.com/.well-known/jwks.json"),
	APPLE("apple", "https://appleid.apple.com", "https://appleid.apple.com/auth/keys");

	private final String description;
	private final String issuer;
	private final String jwksUrl;

	@JsonValue
	public String getDescription() {
		return description;
	}

	@JsonCreator
	public static OAuthProvider from(String description) {
		return Arrays.stream(values())
			.filter(type -> type.getDescription().equals(description))
			.findFirst()
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_SUPPORTED_SOCIAL_TYPE));
	}

}