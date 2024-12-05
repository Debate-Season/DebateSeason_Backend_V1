package com.debateseason_backend_v1.common.response;

import org.springframework.http.HttpStatus;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 응답 공통 형식")
public class ApiResult<T> {

	@Schema(description = "HTTP 상태 코드", example = "200")
	private int status;

	@Schema(description = "응답 코드", example = "SUCCESS")
	private ErrorCode code;

	@Schema(description = "응답 메시지", example = "정상적으로 처리되었습니다")
	private String message;

	@Schema(description = "응답 데이터")
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

	// 200 응답
	public static <T> ApiResult<T> success(String message, T data) {
		return ApiResult.<T>builder()
			.status(HttpStatus.OK.value())
			.code(ErrorCode.SUCCESS)
			.message(message)
			.data(data)
			.build();
	}

	// 에러 응답
	public static <T> ApiResult<T> error(HttpStatus status, ErrorCode errorCode) {
		return ApiResult.<T>builder()
			.status(status.value())
			.code(errorCode)
			.message(errorCode.getMessage())
			.build();
	}

}