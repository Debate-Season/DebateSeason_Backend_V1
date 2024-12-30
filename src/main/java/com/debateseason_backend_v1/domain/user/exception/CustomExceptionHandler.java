package com.debateseason_backend_v1.domain.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;

@ControllerAdvice
public class CustomExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResult<?>> handleCustomException(CustomException ex) {

		ErrorCode errorCode = (ErrorCode)ex.getCodeInterface();
		HttpStatus httpStatus = errorCode.getHttpStatus();

		return ResponseEntity
			.status(httpStatus)
			.body(ApiResult.error(httpStatus, errorCode));
	}

}