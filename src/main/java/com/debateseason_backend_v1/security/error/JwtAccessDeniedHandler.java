package com.debateseason_backend_v1.security.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증은 됐지만 권한이 모자란 요청(USER 가 ADMIN 전용 API 호출)에 대한 응답을 만든다.
 * 기본 핸들러는 빈 본문 403 을 내려 클라이언트가 원인을 알 수 없으므로 JSON 으로 통일한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException
	) throws IOException {

		// 권한 부족은 서버 결함이 아니라 정상적인 거부다. v1.3.2 의 4xx=WARN 규칙을 따른다.
		log.warn("Access denied. [ {} {} ]", request.getMethod(), request.getRequestURI());

		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.ACCESS_DENIED);

		response.setStatus(ErrorCode.ACCESS_DENIED.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
