package com.debateseason_backend_v1.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode implements CodeInterface {

	//result code 는 임의 설정 논의를 통해 변경 될 수 있음 -ksb
	SUCCESS(0, "SUCCESS"),
	DUPLICATE_USERNAME(-1, "DUPLICATE_USERNAME"),
	USER_SAVED_FAILED(-2, "USER_SAVED_FAILED"),
	NOT_EXIST_USER(-3, "NOT_EXIST_USER"),
	EMPTY_PASSWORD(-4, "EMPTY_PASSWORD"),
	MIS_MATCH_PASSWORD(-5, "MIS_MATCH_PASSWORD"),
	INVALID_ROLE(-6, "INVALID_USER_ROLE"),

	// 2000번대 로그인 에러
	INVALID_CREDENTIALS(2000, "아이디 또는 비밀번호가 올바르지 않습니다."),
	USER_NOT_FOUND(2001, "존재하지 않는 사용자입니다."),
	AUTHENTICATION_FAILED(2002, "인증 중 오류가 발생했습니다."),
	;

	private final Integer code;
	private final String message;

}