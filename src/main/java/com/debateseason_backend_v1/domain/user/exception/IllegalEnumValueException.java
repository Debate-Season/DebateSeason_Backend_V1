package com.debateseason_backend_v1.domain.user.exception;

public class IllegalEnumValueException extends RuntimeException {

	public IllegalEnumValueException(String enumType, String value) {

		super(String.format("지원하지 않는 %s 값입니다: %s", enumType, value));
	}

}