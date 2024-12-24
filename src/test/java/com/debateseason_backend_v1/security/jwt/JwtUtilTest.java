package com.debateseason_backend_v1.security.jwt;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.debateseason_backend_v1.common.enums.TokenType;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

@SpringBootTest
class JwtUtilTest {

	@Autowired
	private JwtUtil jwtUtil;

	private final Long TEST_USER_ID = 1L;

	@Nested
	@DisplayName("성공 케이스")
	class SuccessCase {
		@Test
		@DisplayName("Access 토큰 생성 성공")
		void createAccessToken_Success() {
			String accessToken = jwtUtil.createAccessToken(TEST_USER_ID);

			assertThat(accessToken).isNotNull();
			assertThat(jwtUtil.getUserId(accessToken)).isEqualTo(TEST_USER_ID);
			assertThat(jwtUtil.getTokenType(accessToken)).isEqualTo(TokenType.ACCESS);
			assertThat(jwtUtil.isExpired(accessToken)).isFalse();
		}

		@Test
		@DisplayName("Refresh 토큰 생성 성공")
		void createRefreshToken_Success() {
			String refreshToken = jwtUtil.createRefreshToken(TEST_USER_ID);

			assertThat(refreshToken).isNotNull();
			assertThat(jwtUtil.getUserId(refreshToken)).isEqualTo(TEST_USER_ID);
			assertThat(jwtUtil.getTokenType(refreshToken)).isEqualTo(TokenType.REFRESH);
			assertThat(jwtUtil.isExpired(refreshToken)).isFalse();
		}

		@Test
		@DisplayName("토큰 검증 성공")
		void validateToken_Success() {
			String token = jwtUtil.createAccessToken(TEST_USER_ID);

			assertThat(jwtUtil.validate(token)).isTrue();
		}
	}

	@Nested
	@DisplayName("실패 케이스")
	class FailureCase {
		@Test
		@DisplayName("만료된 토큰 검증 실패")
		void validateToken_WhenExpired_ShouldThrow() {
			String expiredToken = jwtUtil.createJwt(TokenType.ACCESS, TEST_USER_ID, -1000L);

			assertThatThrownBy(() -> jwtUtil.validate(expiredToken))
				.isInstanceOf(ExpiredJwtException.class);
			assertThat(jwtUtil.isExpired(expiredToken)).isTrue();
		}

		@Test
		@DisplayName("잘못된 형식의 토큰 검증 실패")
		void validateToken_WhenMalformed_ShouldThrow() {
			String malformedToken = "malformed.jwt.token";

			assertThatThrownBy(() -> jwtUtil.validate(malformedToken))
				.isInstanceOf(MalformedJwtException.class);
		}

		@Test
		@DisplayName("시그니처가 변조된 토큰 검증 실패")
		void validateToken_WhenSignatureInvalid_ShouldThrow() {
			String token = jwtUtil.createAccessToken(TEST_USER_ID) + "invalid";

			assertThatThrownBy(() -> jwtUtil.validate(token))
				.isInstanceOf(SignatureException.class);
		}
	}
	
}