package com.debateseason_backend_v1.security.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationErrorHandler {

	private final ObjectMapper objectMapper;

	// 만료는 설계된 정상 흐름이다. 클라이언트가 401 을 받고 /auth/reissue 로 갱신한다.
	public void handleExpiredToken(HttpServletResponse response, String requestURI) throws IOException {

		log.debug("Token has expired. [ API URI: {} ]", requestURI);

		ErrorResponse errorResponse = ErrorResponse.of(
			ErrorCode.EXPIRED_ACCESS_TOKEN
		);

		writeErrorResponse(response, HttpStatus.UNAUTHORIZED, errorResponse);
	}

	/**
	 * 서명이 현재 시크릿과 맞지 않는 access token.
	 *
	 * <p>거의 전부 <b>휴면 사용자 복귀</b>다 — JWT 시크릿 교체 이전에 발급받은 토큰을 들고
	 * 오랜만에 앱을 켠 경우다. refresh token 은 DB 문자열 매칭으로 통과하므로
	 * ({@code AuthServiceV1#reissueToken}) 클라이언트는 401 → reissue → 재시도로
	 * 1초 안에 스스로 복구한다. 서버 결함이 아니라서 ERROR 로 올리지 않는다.
	 *
	 * <p>다만 서명 불일치는 <b>위조 시도와 구분되지 않는다.</b> 평소에는 산발적인 단발이지만
	 * 짧은 시간에 몰려서 찍히면 공격 신호로 봐야 한다. 그래서 지우지 않고 INFO 로 남긴다.
	 */
	public void handleStaleSignatureToken(HttpServletResponse response, String requestURI) throws IOException {

		log.info("휴면 사용자 복귀 - 구 시크릿으로 서명된 access token, 재발급 유도. [ API URI: {} ]", requestURI);

		ErrorResponse errorResponse = ErrorResponse.of(
			ErrorCode.INVALID_ACCESS_TOKEN
		);
		writeErrorResponse(response, HttpStatus.UNAUTHORIZED, errorResponse);
	}

	// 형식이 깨졌거나 타입이 맞지 않는 토큰. 클라이언트가 보낸 값의 문제다.
	public void handleInvalidToken(HttpServletResponse response, String requestURI) throws IOException {

		log.warn("Invalid token. [ API URI: {} ]", requestURI);

		ErrorResponse errorResponse = ErrorResponse.of(
			ErrorCode.INVALID_ACCESS_TOKEN
		);
		writeErrorResponse(response, HttpStatus.UNAUTHORIZED, errorResponse);
	}

	private void writeErrorResponse(
		HttpServletResponse response,
		HttpStatus status,
		ErrorResponse errorResponse
	) throws IOException {

		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		String jsonResponse = objectMapper.writeValueAsString(errorResponse);
		response.getWriter().write(jsonResponse);
	}

	// 비로그인 클라이언트가 인증 API 를 부른 것. 서버 결함이 아니다.
	public void handleMissingToken(HttpServletResponse response, String requestURI) throws IOException {

		log.debug("Authentication token is missing. [ API URI: {} ]", requestURI);

		ErrorResponse errorResponse = ErrorResponse.of(
			ErrorCode.MISSING_ACCESS_TOKEN
		);

		writeErrorResponse(response, HttpStatus.UNAUTHORIZED, errorResponse);
	}
}