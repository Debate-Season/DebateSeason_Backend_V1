package com.debateseason_backend_v1.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocialType {

	KAKAO("kakao"),
	APPLE("apple");

	private final String description;

}