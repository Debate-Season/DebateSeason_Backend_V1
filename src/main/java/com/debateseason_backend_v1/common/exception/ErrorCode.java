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
	;

	private final Integer code;
	private final String message;

}