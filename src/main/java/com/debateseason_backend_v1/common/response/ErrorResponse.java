package com.debateseason_backend_v1.common.response;

import org.springframework.http.HttpStatus;

import com.debateseason_backend_v1.common.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {

	private int status;
	private ErrorCode code;
	private String message;

	public static ErrorResponse of(HttpStatus httpStatus, ErrorCode code) {
		return new ErrorResponse(httpStatus.value(), code, code.getMessage());
	}

	public static ErrorResponse of(HttpStatus httpStatus, ErrorCode code, String message) {
		return new ErrorResponse(httpStatus.value(), code, message);
	}

}
