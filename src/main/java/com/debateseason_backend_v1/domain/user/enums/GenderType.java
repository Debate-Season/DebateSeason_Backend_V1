package com.debateseason_backend_v1.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenderType {

	MALE("남성"),
	FEMALE("여성"),
	NO_RESPONSE("무응답");

	private final String description;

}