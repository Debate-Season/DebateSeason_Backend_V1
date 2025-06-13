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
	NOT_SUPPORTED_PROVINCE(2015, HttpStatus.BAD_REQUEST, "지원하지 않는 시도입니다."),
	NOT_SUPPORTED_DISTRICT(2016, HttpStatus.BAD_REQUEST, "지원하지 않는 시군구입니다."),
	INVALID_DISTRICT_PROVINCE_RELATION(2017, HttpStatus.BAD_REQUEST, "선택한 시군구가 해당 시도에 속하지 않습니다."),

	// 3000번대 User(인증) 관련 에러
	NOT_FOUND_USER(3000, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
	MISSING_REQUIRED_SOCIAL_ID(3001, HttpStatus.BAD_REQUEST, "소셜 고유 ID는 필수입니다."),
	MISSING_REQUIRED_SOCIAL_TYPE(3002, HttpStatus.BAD_REQUEST, "소셜 타입은 필수입니다."),
	MISSING_REFRESH_TOKEN(3003, HttpStatus.BAD_REQUEST, "Refresh Token은 필수입니다."),
	MISSING_REQUIRED_ID_TOKEN(3004, HttpStatus.BAD_REQUEST, "ID Token은 필수입니다."),
	SOCIAL_TYPE_MISMATCH(3005, HttpStatus.BAD_REQUEST, "ID Token과 소셜 타입이 일치하지 않습니다."),
	INVALID_JWKS_URL(3006, HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 JWKS URL 형식입니다."),
	ID_TOKEN_SIGNATURE_VALIDATION_FAILED(3007, HttpStatus.UNAUTHORIZED, "ID Token 서명 검증에 실패했습니다."),
	ID_TOKEN_DECODING_FAILED(3008, HttpStatus.BAD_REQUEST, "ID Token 디코딩에 실패했습니다."),
	JWKS_NETWORK_ERROR(3009, HttpStatus.SERVICE_UNAVAILABLE, "JWKS 서버와의 네트워크 연결에 실패했습니다."),
	NOT_FOUND_JWKS_KEY(3010, HttpStatus.NOT_FOUND, "요청한 JWKS 키를 찾을 수 없습니다."),
	JWKS_RATE_LIMIT_REACHED(3011, HttpStatus.TOO_MANY_REQUESTS, "JWKS 서버 요청 제한에 도달했습니다."),
	JWKS_RETRIEVAL_FAILED(3012, HttpStatus.INTERNAL_SERVER_ERROR, "JWKS 조회 중 오류가 발생했습니다."),
	PUBLIC_KEY_EXTRACTION_FAILED(3013, HttpStatus.INTERNAL_SERVER_ERROR, "공개키 추출에 실패했습니다."),

	//4000번대 Chat 관련 에러,
	VALUE_OUT_OF_RANGE(4003, HttpStatus.BAD_REQUEST, "메시지 값을 확인해 주세요"),
	NOT_FOUND_MESSAGE(4003, HttpStatus.BAD_REQUEST, "존재 하지 않는 메시지 입니다."),

	// 5000번대 약관 관련 에러
	NOT_FOUND_TERMS(5000, HttpStatus.NOT_FOUND, "존재하지 않는 약관입니다."),
	REQUIRED_TERMS_NOT_AGREED(5001, HttpStatus.BAD_REQUEST, "필수 약관에 모두 동의해야 합니다."),
	ALREADY_AGREED_TERMS(5002, HttpStatus.BAD_REQUEST, "이미 동의한 이용약관입니다."),

	//6000번대 신고 관련 에러
	ALREADY_REPORTED(6001, HttpStatus.CONFLICT, "이미 신고된 메시지 입니다."),
	REPORT_ALREADY_PROCESSED(6002, HttpStatus.CONFLICT, "이미 처리된 신고는 상태를 변경할 수 없습니다."),
	SELF_REPORT_NOT_ALLOWED(6003, HttpStatus.BAD_REQUEST, "자신의 메시지는 신고할 수 없습니다."),

	// 7000번대: 동시성 관련 에러
	CONCURRENT_REQUEST(7000, HttpStatus.CONFLICT, "요청 처리 중 충돌이 발생했습니다. 잠시 후 다시 시도해주세요."),

	NOT_FOUND_REPORT(404, HttpStatus.NOT_FOUND, "NOT_FOUND"),

	// API 요청 에러
	BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

	// 기타
	// Issue방 관련 에러

	// 400번대
	NOT_FOUND_ISSUE(404, HttpStatus.NOT_FOUND, "주어진 id값에 해당하는 이슈방을 찾을 수 없습니다."),
	NOT_FOUND_CHATROOM(404, HttpStatus.NOT_FOUND, "주어진 chatroomId로 해당하는 채팅방을 찾을 수 없습니다."),
	NOT_FOUND_ISSUE_WITH_CATEGORY(400, HttpStatus.NOT_FOUND, "해당 category의 이슈방을 찾을 수 없습니다."),

	// 페이지네이션 오류
	PAGE_OUT_OF_RANGE(404, HttpStatus.NOT_FOUND, "검색범위를 넘어섰습니다. 내용을 불러올 수 없습니다."),

	// Media 관련 오류
	MEDIA_NOT_FOUND(404, HttpStatus.NOT_FOUND, "요청하신 미디어는 존재하지 않습니다.");

	private final Integer code;
	private final HttpStatus httpStatus;
	private final String message;

}
