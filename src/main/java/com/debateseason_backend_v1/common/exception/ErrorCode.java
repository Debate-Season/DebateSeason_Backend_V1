package com.debateseason_backend_v1.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter

public enum ErrorCode implements CodeInterface {

	//result code 는 임의 설정 논의를 통해 변경 될 수 있음 -ksb
	SUCCESS(0, HttpStatus.OK, "SUCCESS"),
	USER_ALREADY_EXISTS(-1, HttpStatus.CONFLICT, "USER_ALREADY_EXISTS"),
	USER_SAVED_FAILED(-2, HttpStatus.INTERNAL_SERVER_ERROR, "USER_SAVED_FAILED"),
	NOT_EXIST_USER(-3, HttpStatus.NOT_FOUND, "NOT_EXIST_USER"),
	MIS_MATCH_PASSWORD(-4, HttpStatus.UNAUTHORIZED, "MIS_MATCH_PASSWORD"),

	// 1000번대 JWT 에러
	TOKEN_EXPIRED(1000, HttpStatus.UNAUTHORIZED, "인증이 만료되었습니다. 다시 로그인해주세요."),
	INVALID_TOKEN(1001, HttpStatus.UNAUTHORIZED, "유효하지 않은 인증입니다."),

	// 2000번대 프로필 관련 에러
	NOT_SUPPORTED_COMMUNITY(2000, HttpStatus.BAD_REQUEST, "지원하지 않는 커뮤니티입니다."),
	INVALID_NICKNAME_FORMAT(2001, HttpStatus.BAD_REQUEST, "닉네임은 한글 또는 영문으로 8자 이내로 입력해주세요."),
	DUPLICATE_NICKNAME(2002, HttpStatus.CONFLICT, "중복된 닉네임입니다."),
	NOT_EXIST_PROFILE(2003, HttpStatus.NOT_FOUND, "프로필이 존재하지 않습니다."),
	NOT_EXIST_PROFILE_COMMUNITY(2004, HttpStatus.NOT_FOUND, "프로필의 커뮤니티 정보가 없습니다."),
	ALREADY_EXIST_PROFILE(2005, HttpStatus.CONFLICT, "이미 프로필이 등록된 사용자입니다.");

	private final Integer code;
	private final HttpStatus httpStatus;
	private final String message;

}
