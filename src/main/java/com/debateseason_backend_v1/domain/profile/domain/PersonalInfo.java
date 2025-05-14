package com.debateseason_backend_v1.domain.profile.domain;

import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;

public record PersonalInfo(
	String profileImage,
	Nickname nickname,
	GenderType gender,
	AgeRangeType ageRange
) {

	public PersonalInfo {
		if (nickname == null) {
			throw new IllegalArgumentException("ActiveNickname can't be null");
		}

		if (gender == null) {
			throw new IllegalArgumentException("Gender can't be null");
		}

		if (ageRange == null) {
			throw new IllegalArgumentException("AgeRange can't be null");
		}
	}

}
