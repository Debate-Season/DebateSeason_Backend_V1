package com.debateseason_backend_v1.common.response;

import org.springframework.http.HttpStatus;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "API 공통 응답 형식")
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

	@Schema(description = "HTTP 상태 코드", example = "200", implementation = Integer.class)
	private int status;

	@Schema(description = "응답 코드", example = "SUCCESS", implementation = ErrorCode.class)
	private ErrorCode code;

	@Schema(description = "응답 메시지", example = "정상적으로 처리되었습니다")
	private String message;

	@Schema(description = "응답 데이터", implementation = Object.class)
	private T data;
	
	// 커스텀 status 성공 응답
	public static <T> ApiResult<T> of(HttpStatus status, String message, T data) {

		return ApiResult.<T>builder()
			.status(status.value())
			.code(ErrorCode.SUCCESS)
			.message(message)
			.data(data)
			.build();
	}

	// 데이터가 없는 성공 응답
	public static ApiResult<Void> success(String message) {
		return ApiResult.<Void>builder()
			.status(HttpStatus.OK.value())
			.code(ErrorCode.SUCCESS)
			.message(message)
			.build();
	}

	// 데이터가 있는 성공 응답
	public static <T> ApiResult<T> success(String message, T data) {

		return ApiResult.<T>builder()
			.status(HttpStatus.OK.value())
			.code(ErrorCode.SUCCESS)
			.message(message)
			.data(data)
			.build();
	}

}