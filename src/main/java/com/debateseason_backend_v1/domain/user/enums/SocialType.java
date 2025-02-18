package com.debateseason_backend_v1.domain.user.enums;

import java.util.Arrays;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocialType {

	KAKAO("kakao"),
	APPLE("apple");

	private final String description;

	@JsonValue
	public String getDescription() {
		return description;
	}

	@JsonCreator
	public static SocialType from(String description) {
		return Arrays.stream(values())
			.filter(type -> type.getDescription().equals(description))
			.findFirst()
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_SUPPORTED_SOCIAL_TYPE));
	}

}