package com.debateseason_backend_v1.domain.user.enums;

import java.util.Arrays;

import com.debateseason_backend_v1.domain.user.exception.IllegalEnumValueException;
import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenderType {

	MALE("남성"),
	FEMALE("여성"),
	NO_RESPONSE("무응답");

	private final String description;

	@JsonCreator
	public static GenderType from(String description) {
		return Arrays.stream(values())
			.filter(type -> type.getDescription().equals(description))
			.findFirst()
			.orElseThrow(() -> new IllegalEnumValueException("GenderType", description));
	}

}