package com.debateseason_backend_v1.domain.profile.domain;

import java.util.Arrays;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.profile.enums.CommunityType;

public record CommunityId(
	Long value
) {

	public CommunityId {
		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}

		if (value < 0) {
			throw new IllegalArgumentException("value must be positive");
		}

		boolean exists = Arrays.stream(CommunityType.values())
			.anyMatch(type -> type.getId().equals(value));

		if (!exists) {
			throw new CustomException(ErrorCode.NOT_SUPPORTED_COMMUNITY);
		}
	}
}
