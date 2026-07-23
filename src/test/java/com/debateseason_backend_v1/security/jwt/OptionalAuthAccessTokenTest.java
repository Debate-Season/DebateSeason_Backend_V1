package com.debateseason_backend_v1.security.jwt;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.enums.TokenType;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.user.domain.UserRole;
import com.debateseason_backend_v1.security.component.SecurityPathMatcher;
import com.debateseason_backend_v1.security.error.JwtAuthenticationErrorHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

/**
 * optional-auth 조회 엔드포인트의 토큰 처리 정합성 검증.
 *
 * <p>버그: 만료/무효 토큰을 실어 보내면 401 이 아니라 200 + 익명 응답으로 조용히 강등돼
 * 클라이언트의 401 기반 리프레시가 트리거되지 않았다. 아래 계약을 고정한다.
 * <ul>
 *   <li>헤더 없음(진짜 비로그인) -> 200 (익명 응답 유지)</li>
 *   <li>토큰 만료 -> 401 EXPIRED_ACCESS_TOKEN</li>
 *   <li>토큰 서명 불일치/형식 오류 -> 401 INVALID_ACCESS_TOKEN</li>
 * </ul>
 */
class OptionalAuthAccessTokenTest {

	private JwtUtil jwtUtil;
	private MockMvc mockMvc;

	private static final String TOKEN = "test.jwt.token";
	private static final String OPTIONAL_URL = "/api/v1/room";

	@BeforeEach
	void setUp() {
		SecurityContextHolder.clearContext();
		jwtUtil = Mockito.mock(JwtUtil.class);

		JwtAuthenticationErrorHandler errorHandler =
			new JwtAuthenticationErrorHandler(new ObjectMapper());
		SecurityPathMatcher pathMatcher = new SecurityPathMatcher();
		JwtAuthenticationFilter filter =
			new JwtAuthenticationFilter(jwtUtil, errorHandler, pathMatcher);

		mockMvc = MockMvcBuilders.standaloneSetup(new StubController())
			.addFilters(filter)
			.build();
	}

	@Test
	@DisplayName("헤더가 없으면 익명으로 200 응답 (순수 비로그인 플로우 보존)")
	void anonymousWhenNoHeader() throws Exception {
		mockMvc.perform(get(OPTIONAL_URL))
			.andExpect(status().isOk());

		verify(jwtUtil, never()).validate(any());
	}

	@Test
	@DisplayName("유효한 토큰이면 인증되어 200 응답")
	void authenticatedWhenValidToken() throws Exception {
		given(jwtUtil.validate(TOKEN)).willReturn(true);
		given(jwtUtil.getTokenType(TOKEN)).willReturn(TokenType.ACCESS);
		given(jwtUtil.getUserId(TOKEN)).willReturn(1L);
		given(jwtUtil.getRole(TOKEN)).willReturn(UserRole.USER);

		mockMvc.perform(get(OPTIONAL_URL)
				.header("Authorization", "Bearer " + TOKEN))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("만료된 토큰이면 익명 강등 대신 401 EXPIRED_ACCESS_TOKEN")
	void unauthorizedWhenExpiredToken() throws Exception {
		willThrow(new ExpiredJwtException(null, null, "expired"))
			.given(jwtUtil).validate(TOKEN);

		mockMvc.perform(get(OPTIONAL_URL)
				.header("Authorization", "Bearer " + TOKEN))
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
			.andExpect(jsonPath("$.code").value(ErrorCode.EXPIRED_ACCESS_TOKEN.name()));
	}

	@Test
	@DisplayName("서명이 불일치하는 토큰이면 401 INVALID_ACCESS_TOKEN")
	void unauthorizedWhenInvalidSignature() throws Exception {
		willThrow(new SignatureException("invalid signature"))
			.given(jwtUtil).validate(TOKEN);

		mockMvc.perform(get(OPTIONAL_URL)
				.header("Authorization", "Bearer " + TOKEN))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.INVALID_ACCESS_TOKEN.name()));
	}

	@Test
	@DisplayName("Bearer 접두사가 없는 헤더면 401 INVALID_ACCESS_TOKEN")
	void unauthorizedWhenNoBearerPrefix() throws Exception {
		mockMvc.perform(get(OPTIONAL_URL)
				.header("Authorization", "Basic " + TOKEN))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.INVALID_ACCESS_TOKEN.name()));

		verify(jwtUtil, never()).validate(any());
	}

	@RestController
	static class StubController {

		@GetMapping(OPTIONAL_URL)
		ResponseEntity<String> room() {
			return ResponseEntity.ok("ok");
		}
	}
}
