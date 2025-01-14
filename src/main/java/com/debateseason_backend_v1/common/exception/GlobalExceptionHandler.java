package com.debateseason_backend_v1.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.debateseason_backend_v1.common.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ResponseEntity<ErrorResponse> handle(HttpRequestMethodNotSupportedException e) {

		log.error("HttpRequestMethodNotSupportedException", e);
		return createErrorResponseEntity(ErrorCode.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {

		log.error("CustomException occurred. ErrorCode: {}, Message: {}",
			e.getCodeInterface().getCode(),
			e.getMessage(),
			e);

		ErrorCode errorCode = (ErrorCode)e.getCodeInterface();

		return createErrorResponseEntity(errorCode);
	}

	private ResponseEntity<ErrorResponse> createErrorResponseEntity(ErrorCode errorCode) {

		return new ResponseEntity<>(
			ErrorResponse.of(errorCode.getHttpStatus(), errorCode),
			errorCode.getHttpStatus());
	}

}