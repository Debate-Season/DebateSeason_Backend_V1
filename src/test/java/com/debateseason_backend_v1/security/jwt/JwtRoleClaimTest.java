package com.debateseason_backend_v1.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.common.enums.TokenType;
import com.debateseason_backend_v1.domain.user.domain.UserRole;

@DisplayName("JWT role 클레임")
class JwtRoleClaimTest {

	private static final Long TEST_USER_ID = 1L;
	private static final long EXPIRE_TIME = 600_000L;

	private final JwtUtil jwtUtil = new JwtUtil(
		"test-secret-key-for-jwt-role-claim-test-1234567890",
		EXPIRE_TIME,
		EXPIRE_TIME
	);

	@Test
	@DisplayName("ADMIN 으로 발급한 토큰은 ADMIN 으로 읽힌다")
	void adminTokenKeepsRole() {
		String token = jwtUtil.createAccessToken(TEST_USER_ID, UserRole.ADMIN);

		assertThat(jwtUtil.getRole(token)).isEqualTo(UserRole.ADMIN);
	}

	@Test
	@DisplayName("USER 로 발급한 토큰은 USER 로 읽힌다")
	void userTokenKeepsRole() {
		String token = jwtUtil.createAccessToken(TEST_USER_ID, UserRole.USER);

		assertThat(jwtUtil.getRole(token)).isEqualTo(UserRole.USER);
	}

	@Test
	@DisplayName("role 클레임이 없는 구버전 토큰은 USER 로 간주한다")
	void legacyTokenWithoutRoleClaimFallsBackToUser() {
		// 구버전 앱이 이미 발급받아 들고 있는 토큰을 재현한다 (role 클레임 없음)
		String legacyToken = jwtUtil.createJwt(TokenType.ACCESS, TEST_USER_ID, EXPIRE_TIME, null);

		assertThat(jwtUtil.getRole(legacyToken)).isEqualTo(UserRole.USER);
	}

	@Test
	@DisplayName("알 수 없는 role 값은 USER 로 격하한다")
	void unknownRoleFallsBackToUser() {
		assertThat(UserRole.fromNullable("SUPER_ADMIN")).isEqualTo(UserRole.USER);
		assertThat(UserRole.fromNullable("")).isEqualTo(UserRole.USER);
		assertThat(UserRole.fromNullable(null)).isEqualTo(UserRole.USER);
	}

	@Test
	@DisplayName("refresh token 에는 role 을 싣지 않는다")
	void refreshTokenHasNoRole() {
		String refreshToken = jwtUtil.createRefreshToken(TEST_USER_ID);

		assertThat(jwtUtil.getRole(refreshToken)).isEqualTo(UserRole.USER);
	}
}
