package com.debateseason_backend_v1.domain.profile.domain;

import java.util.regex.Pattern;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;

public record Nickname(
	String value
) {
	private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]{1,8}$");

	public Nickname {
		if (value == null) {
			throw new CustomException(ErrorCode.REQUIRED_NICKNAME);
		}

		if (value.isBlank()) {
			throw new CustomException(ErrorCode.REQUIRED_NICKNAME);
		}
	}

	public static Nickname of(String value) {
		if (!NICKNAME_PATTERN.matcher(value).matches()) {
			throw new CustomException(ErrorCode.INVALID_NICKNAME_PATTERN);
		}
		return new Nickname(value);
	}

	public static Nickname anonymize(String uuid) {
		return new Nickname("탈퇴회원#" + uuid);
	}

}
