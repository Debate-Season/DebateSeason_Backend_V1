package com.debateseason_backend_v1.common.response;

import org.springframework.http.HttpStatus;

import com.debateseason_backend_v1.common.exception.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "데이터가 없는 API 응답")
@Getter
@Builder
@AllArgsConstructor
public class VoidApiResult {
	@Schema(description = "HTTP 상태 코드", example = "200")
	private int status;

	@Schema(description = "응답 코드", example = "SUCCESS")
	private ErrorCode code;

	@Schema(description = "응답 메시지", example = "정상적으로 처리되었습니다.")
	private String message;

	public static VoidApiResult success(String message) {
		
		return VoidApiResult.builder()
			.status(HttpStatus.OK.value())
			.code(ErrorCode.SUCCESS)
			.message(message)
			.build();
	}

}