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

	public void handleExpiredToken(HttpServletResponse response, String requestURI) throws IOException {

		log.error("Token has expired. [ API URI: {} ]", requestURI);

		ErrorResponse errorResponse = ErrorResponse.of(
			ErrorCode.EXPIRED_ACCESS_TOKEN
		);

		writeErrorResponse(response, HttpStatus.UNAUTHORIZED, errorResponse);
	}

	public void handleInvalidToken(HttpServletResponse response, String requestURI) throws IOException {

		log.error("Invalid token. [ API URI: {} ]", requestURI);

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

	public void handleMissingToken(HttpServletResponse response, String requestURI) throws IOException {

		log.error("Authentication token is missing. [ API URI: {} ]", requestURI);

		ErrorResponse errorResponse = ErrorResponse.of(
			ErrorCode.MISSING_ACCESS_TOKEN
		);

		writeErrorResponse(response, HttpStatus.UNAUTHORIZED, errorResponse);
	}
}