package com.debateseason_backend_v1.domain.profile.enums;

import java.util.Arrays;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AgeRangeType {

	TEENAGER("10대", 10, 19),
	TWENTIES("20대", 20, 29),
	THIRTIES("30대", 30, 39),
	FORTIES("40대", 40, 49),
	FIFTIES("50대", 50, 59),
	SIXTIES("60대", 60, 69),
	SEVENTIES("70대", 70, 79),
	EIGHTIES("80대", 80, 89),
	OVER_NINETY("90대 이상", 90, 120);

	private final String description;
	private final int minAge;
	private final int maxAge;

	@JsonValue
	public String getDescription() {
		return description;
	}

	@JsonCreator
	public static AgeRangeType from(String description) {
		return Arrays.stream(values())
			.filter(type -> type.getDescription().equals(description))
			.findFirst()
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_SUPPORTED_AGE_RANGE));
	}

}