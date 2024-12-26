package com.debateseason_backend_v1.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocialType {

	KAKAO("kakao"),
	APPLE("apple");

	private final String value;

	@JsonCreator
	public static SocialType fromValue(String value) {
		for (SocialType socialType : SocialType.values()) {
			if (socialType.value.equalsIgnoreCase(value)) {
				return socialType;
			}
		}
		throw new IllegalArgumentException("Invalid social type: " + value);
	}
}