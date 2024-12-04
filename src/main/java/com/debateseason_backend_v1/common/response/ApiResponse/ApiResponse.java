package com.debateseason_backend_v1.common.response.ApiResponse;

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
public class ApiResponse<T> {

	@Schema(description = "HTTP 상태 코드", example = "200")
	private int status;

	@Schema(description = "응답 코드", example = "SUCCESS")
	private ErrorCode code;

	@Schema(description = "응답 메시지", example = "정상적으로 처리되었습니다")
	private String message;

	@Schema(description = "응답 데이터")
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