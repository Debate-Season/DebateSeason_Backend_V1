package com.debateseason_backend_v1.common.response.ApiResponse;

import org.springframework.http.HttpStatus;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private int status;
	private ErrorCode code;
	private String message;
	private T data;

	public static <T> ApiResponse<T> success(String message, T data) {
		return ApiResponse.<T>builder()
			.status(HttpStatus.OK.value())
			.code(ErrorCode.SUCCESS)
			.message(message)
			.data(data)
			.build();
	}

	public static <T> ApiResponse<T> error(HttpStatus status, ErrorCode errorCode) {
		return ApiResponse.<T>builder()
			.status(status.value())
			.code(errorCode)
			.message(errorCode.getMessage())
			.build();
	}
	
}