package com.debateseason_backend_v1.domain.user.enums;

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

}