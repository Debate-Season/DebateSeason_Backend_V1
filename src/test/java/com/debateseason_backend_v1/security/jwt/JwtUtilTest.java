package com.debateseason_backend_v1.security.jwt;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.debateseason_backend_v1.common.enums.TokenType;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.user.domain.UserRole;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

@SpringBootTest
@ActiveProfiles("test")
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
			String accessToken = jwtUtil.createAccessToken(TEST_USER_ID, UserRole.USER);

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
			String token = jwtUtil.createAccessToken(TEST_USER_ID, UserRole.USER);

			assertThat(jwtUtil.validate(token)).isTrue();
		}
	}

	@Nested
	@DisplayName("실패 케이스")
	class FailureCase {
		@Test
		@DisplayName("만료된 토큰 검증 실패")
		void validateToken_WhenExpired_ShouldThrow() {
			String expiredToken = jwtUtil.createJwt(TokenType.ACCESS, TEST_USER_ID, -1000L, UserRole.USER);

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
			String token = jwtUtil.createAccessToken(TEST_USER_ID, UserRole.USER) + "invalid";

			assertThatThrownBy(() -> jwtUtil.validate(token))
				.isInstanceOf(SignatureException.class);
		}
	}

	/**
	 * 재발급·로그아웃이 공유하는 refresh token 검증.
	 * 예전에는 재발급이 DB 문자열 매칭만 해서 만료·서명이 사실상 무의미했다.
	 */
	@Nested
	@DisplayName("Refresh 토큰 검증")
	class ValidateRefreshToken {

		@Test
		@DisplayName("정상 refresh 토큰은 통과한다")
		void pass() {
			String refreshToken = jwtUtil.createRefreshToken(TEST_USER_ID);

			assertThatCode(() -> jwtUtil.validateRefreshToken(refreshToken))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("만료된 refresh 토큰은 EXPIRED_REFRESH_TOKEN")
		void expired() {
			String expired = jwtUtil.createJwt(TokenType.REFRESH, TEST_USER_ID, -1000L, null);

			assertThatThrownBy(() -> jwtUtil.validateRefreshToken(expired))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.EXPIRED_REFRESH_TOKEN);
		}

		@Test
		@DisplayName("서명이 맞지 않는 refresh 토큰은 INVALID_REFRESH_TOKEN (500 이 아니다)")
		void badSignature() {
			String tampered = jwtUtil.createRefreshToken(TEST_USER_ID) + "invalid";

			assertThatThrownBy(() -> jwtUtil.validateRefreshToken(tampered))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.INVALID_REFRESH_TOKEN);
		}

		@Test
		@DisplayName("access 토큰으로는 재발급할 수 없다")
		void rejectsAccessToken() {
			String accessToken = jwtUtil.createAccessToken(TEST_USER_ID, UserRole.USER);

			assertThatThrownBy(() -> jwtUtil.validateRefreshToken(accessToken))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.INVALID_REFRESH_TOKEN);
		}
	}

}