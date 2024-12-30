package com.debateseason_backend_v1.security.jwt;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.enums.TokenType;
import com.debateseason_backend_v1.common.exception.ErrorCode;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtAuthenticationFilterTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private JwtUtil jwtUtil;

	private final String TEST_TOKEN = "test.jwt.token";
	private final Long TEST_USER_ID = 1L;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.clearContext();
	}

	@Nested
	@DisplayName("성공 케이스")
	class SuccessCase {
		@Test
		@DisplayName("유효한 액세스 토큰으로 인증 성공")
		void authenticateWithValidAccessToken() throws Exception {
			// given
			String bearerToken = "Bearer " + TEST_TOKEN;
			given(jwtUtil.validate(TEST_TOKEN)).willReturn(true);
			given(jwtUtil.getTokenType(TEST_TOKEN)).willReturn(TokenType.ACCESS);
			given(jwtUtil.getUserId(TEST_TOKEN)).willReturn(TEST_USER_ID);

			// when & then
			mockMvc.perform(get("/api/authenticate")
					.header("Authorization", bearerToken))
				.andExpect(status().isOk())
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("실패 케이스")
	class FailureCase {
		@Test
		@DisplayName("만료된 토큰으로 인증 실패")
		void failWithExpiredToken() throws Exception {
			// given
			String bearerToken = "Bearer " + TEST_TOKEN;
			willThrow(new ExpiredJwtException(null, null, "expired"))
				.given(jwtUtil).validate(TEST_TOKEN);

			// when & then
			mockMvc.perform(get("/api/authenticate")
					.header("Authorization", bearerToken))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
				.andExpect(jsonPath("$.code").value(ErrorCode.TOKEN_EXPIRED.name()))
				.andDo(print());
		}

		@Test
		@DisplayName("유효하지 않은 토큰으로 인증 실패")
		void failWithInvalidToken() throws Exception {
			// given
			String bearerToken = "Bearer " + TEST_TOKEN;
			willThrow(new SignatureException("invalid signature"))
				.given(jwtUtil).validate(TEST_TOKEN);

			// when & then
			mockMvc.perform(get("/api/authenticate")
					.header("Authorization", bearerToken))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
				.andExpect(jsonPath("$.code").value(ErrorCode.INVALID_TOKEN.name()))
				.andDo(print());
		}

		@Test
		@DisplayName("Bearer 접두사가 없는 토큰으로 인증 실패")
		void failWithoutBearerPrefix() throws Exception {
			// given
			String invalidToken = "Invalid " + TEST_TOKEN;

			// when & then
			mockMvc.perform(get("/api/authenticate")
					.header("Authorization", invalidToken))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
				.andExpect(jsonPath("$.code").value(ErrorCode.INVALID_TOKEN.name()))
				.andDo(print());

			// JWT 검증이 실행되지 않았음을 확인
			verify(jwtUtil, never()).validate(any());
		}
	}

	@RestController
	@RequestMapping("/api")
	public static class TestController {

		@GetMapping("/authenticate")
		public ResponseEntity<String> authenticateApi() {
			return ResponseEntity.ok("test");
		}

		@GetMapping("/v1/permit")
		public ResponseEntity<String> permitApi() {
			return ResponseEntity.ok("test");
		}
	}
}
