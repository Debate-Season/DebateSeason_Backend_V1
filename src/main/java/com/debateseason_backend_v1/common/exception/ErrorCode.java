package com.debateseason_backend_v1.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter

public enum ErrorCode implements CodeInterface {

	//result code 는 임의 설정 논의를 통해 변경 될 수 있음 -ksb
	SUCCESS(0, "SUCCESS"),
	USER_ALREADY_EXISTS(-1, "USER_ALREADY_EXISTS"),
	USER_SAVED_FAILED(-2, "USER_SAVED_FAILED"),
	NOT_EXIST_USER(-3, "NOT_EXIST_USER"),
	MIS_MATCH_PASSWORD(-4, "MIS_MATCH_PASSWORD"),

	// 1000번대 JWT 에러
	TOKEN_EXPIRED(1000, "인증이 만료되었습니다. 다시 로그인해주세요."),
	INVALID_TOKEN(1001, "유효하지 않은 인증입니다.");;;

	private final Integer code;
	private final String message;

}
