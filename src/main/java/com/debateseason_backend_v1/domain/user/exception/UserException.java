package com.debateseason_backend_v1.domain.user.exception;

import com.debateseason_backend_v1.common.exception.CodeInterface;
import com.debateseason_backend_v1.common.exception.CustomException;

public class UserException extends CustomException {
	
	public UserException(CodeInterface errorCode) {
		super(errorCode);
	}

	public UserException(CodeInterface errorCode, String message) {
		super(errorCode, message);
	}
}
