package com.debateseason_backend_v1.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements CodeInterface {

	//result code 는 임의 설정 논의를 통해 변경 될 수 있음 -ksb
	SUCCESS(0, HttpStatus.OK, "SUCCESS"),
	USER_ALREADY_EXISTS(-1, HttpStatus.CONFLICT, "USER_ALREADY_EXISTS"),
	USER_SAVED_FAILED(-2, HttpStatus.INTERNAL_SERVER_ERROR, "USER_SAVED_FAILED"),
	NOT_EXIST_USER(-3, HttpStatus.NOT_FOUND, "NOT_EXIST_USER"),
	MIS_MATCH_PASSWORD(-4, HttpStatus.UNAUTHORIZED, "MIS_MATCH_PASSWORD"),
	NOT_FOUND(-5, HttpStatus.NOT_FOUND, "NOT_FOUND"),
	DUPLICATE(-7, HttpStatus.CONFLICT, "DUPLICATE"),
	METHOD_NOT_ALLOWED(-8, HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED"),
	INTERNAL_SERVER_ERROR(-9, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR"),

	// 1000번대 JWT 에러
	MISSING_ACCESS_TOKEN(1002, HttpStatus.UNAUTHORIZED, "Access Token이 필요합니다."),
	EXPIRED_ACCESS_TOKEN(1000, HttpStatus.UNAUTHORIZED, "Access Token이 만료되었습니다."),
	EXPIRED_REFRESH_TOKEN(1004, HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었습니다."),
	INVALID_ACCESS_TOKEN(1001, HttpStatus.UNAUTHORIZED, "유효하지 않은 Access Token입니다."),
	INVALID_REFRESH_TOKEN(1003, HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."),

	// 2000번대 프로필 관련 에러
	INVALID_NICKNAME_PATTERN(2001, HttpStatus.BAD_REQUEST, "닉네임은 한글 또는 영문으로 8자 이내로 입력해주세요."),
	DUPLICATE_NICKNAME(2002, HttpStatus.CONFLICT, "중복된 닉네임입니다."),
	NOT_FOUND_PROFILE(2003, HttpStatus.NOT_FOUND, "프로필 정보를 찾을 수 없습니다."),
	NOT_FOUND_COMMUNITY(2004, HttpStatus.NOT_FOUND, "커뮤니티 정보를 찾을 수 없습니다."),
	NOT_FOUND_COMMUNITY_MEMBERSHIP(2005, HttpStatus.NOT_FOUND, "커뮤니티 가입 정보를 찾을 수 없습니다."),
	ALREADY_EXIST_PROFILE(2006, HttpStatus.CONFLICT, "이미 프로필이 등록된 사용자입니다."),
	NOT_SUPPORTED_GENDER_TYPE(2007, HttpStatus.BAD_REQUEST, "지원하지 않는 성별 타입입니다"),
	NOT_SUPPORTED_AGE_RANGE(2008, HttpStatus.BAD_REQUEST, "지원하지 않는 연령대입니다"),
	NOT_SUPPORTED_COMMUNITY(2009, HttpStatus.BAD_REQUEST, "지원하지 않는 커뮤니티입니다."),
	NOT_SUPPORTED_SOCIAL_TYPE(2010, HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 타입입니다"),
	MISSING_REQUIRED_NICKNAME(2011, HttpStatus.BAD_REQUEST, "닉네임은 필수입니다."),
	MISSING_REQUIRED_COMMUNITY(2012, HttpStatus.BAD_REQUEST, "커뮤니티 선택은 필수입니다."),
	MISSING_REQUIRED_GENDER_TYPE(2013, HttpStatus.BAD_REQUEST, "성별은 선택은 필수입니다."),
	MISSING_REQUIRED_AGE_RANGE(2014, HttpStatus.BAD_REQUEST, "연령대는 선택은 필수입니다."),

	// 3000번대 User 관련 에러
	NOT_FOUND_USER(3000, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
	MISSING_REQUIRED_SOCIAL_ID(3001, HttpStatus.BAD_REQUEST, "소셜 고유 ID는 필수입니다."),
	MISSING_REQUIRED_SOCIAL_TYPE(3002, HttpStatus.BAD_REQUEST, "소셜 타입은 필수입니다."),
	MISSING_REFRESH_TOKEN(3003, HttpStatus.BAD_REQUEST, "Refresh Token은 필수입니다."),

	//4000번대 Chat 관련 에러,
	VALUE_OUT_OF_RANGE(3003, HttpStatus.BAD_REQUEST, "메시지 값을 확인해 주세요"),

	// API 요청 에러
	BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");

	private final Integer code;
	private final HttpStatus httpStatus;
	private final String message;

}
