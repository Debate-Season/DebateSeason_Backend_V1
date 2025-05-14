package com.debateseason_backend_v1.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

	private final CodeInterface codeInterface;

	public CustomException(CodeInterface errorCode) {
		super(errorCode.getMessage());
		this.codeInterface = errorCode;
	}

	public CustomException(CodeInterface errorCode, String message) {
		super(message);
		this.codeInterface = errorCode;
	}

}
