package com.debateseason_backend_v1.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.auth.service.request.TokenReissueServiceRequest;
import com.debateseason_backend_v1.domain.auth.service.response.TokenReissueResponse;
import com.debateseason_backend_v1.domain.repository.RefreshTokenRepository;
import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;
import com.debateseason_backend_v1.domain.user.infrastructure.UserJpaRepository;
import com.debateseason_backend_v1.security.jwt.JwtUtil;

/**
 * refresh token 재발급의 불변식을 고정한다.
 *
 * 이 경로는 2026-08-09 에 한 번 통째로 다시 썼다 — 그 전에는 토큰을 파싱조차 하지 않고
 * DB 문자열 매칭만 해서 REFRESH_EXPIRE_TIME 이 아무 효력이 없었고, 시크릿을 교체해도
 * 기존 세션을 끊을 수 없었다. 그런데 지금까지 테스트가 하나도 없었다.
 *
 * 특히 '한 유저가 refresh_tokens 행을 여러 개 갖는다' 는 성질을 고정한다. 운영 데이터만
 * 보면 중복처럼 보여서 user_id 에 유니크 제약을 걸고 싶어지는데, 그러면 기기 두 대가
 * 서로의 토큰을 밀어내 한쪽이 조용히 로그아웃된다. 실제로 2026-09-04 조사 중에
 * 그렇게 '고칠' 뻔했다. 행 1개 = 로그인 1회 = 기기별 세션이 맞는 설계다.
 */
class AuthServiceV1ReissueTest {

	private static final Long USER_ID = 9L;
	private static final String PHONE_TOKEN = "phone.refresh.token";
	private static final String LAPTOP_TOKEN = "laptop.refresh.token";

	private final JwtUtil jwtUtil = mock(JwtUtil.class);
	private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
	private final UserJpaRepository userJpaRepository = mock(UserJpaRepository.class);

	private final AuthServiceV1 authService = new AuthServiceV1(
		jwtUtil, refreshTokenRepository, userJpaRepository
	);

	private TokenReissueServiceRequest request(String token) {
		return new TokenReissueServiceRequest(token);
	}

	private RefreshToken row(String current, String previous) {
		RefreshToken row = RefreshToken.create(USER_ID, current, previous);
		// @LastModifiedDate 는 JPA 감사(auditing)가 채우므로 순수 단위 테스트에서는 null 이다.
		// grace 분기가 updatedAt 을 읽으므로 명시적으로 넣어준다.
		setUpdatedAt(row, LocalDateTime.now());
		return row;
	}

	private void setUpdatedAt(RefreshToken row, LocalDateTime value) {
		try {
			var field = RefreshToken.class.getDeclaredField("updatedAt");
			field.setAccessible(true);
			field.set(row, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	@Test
	@DisplayName("정상 재발급은 토큰을 회전시킨다 — 쓰던 토큰이 previous 로 밀린다")
	void rotatesOnReissue() {
		RefreshToken stored = row(PHONE_TOKEN, "older.token");
		given(refreshTokenRepository.findByCurrentTokenOrPreviousToken(PHONE_TOKEN))
			.willReturn(Optional.of(stored));
		given(jwtUtil.createAccessToken(eq(USER_ID), any())).willReturn("new.access");
		given(jwtUtil.createRefreshToken(USER_ID)).willReturn("rotated.refresh");

		TokenReissueResponse response = authService.reissueToken(request(PHONE_TOKEN));

		assertThat(response.refreshToken()).isEqualTo("rotated.refresh");
		assertThat(stored.getCurrentToken()).isEqualTo("rotated.refresh");
		assertThat(stored.getPreviousToken()).isEqualTo(PHONE_TOKEN);
	}

	@Test
	@DisplayName("만료·위조 토큰은 DB 를 긁기 전에 막는다")
	void rejectsInvalidTokenBeforeTouchingDb() {
		willThrow(new CustomException(ErrorCode.INVALID_REFRESH_TOKEN))
			.given(jwtUtil).validateRefreshToken(PHONE_TOKEN);

		assertThatThrownBy(() -> authService.reissueToken(request(PHONE_TOKEN)))
			.isInstanceOf(CustomException.class);

		// 검증이 조회보다 먼저다. 만료 토큰으로 테이블을 긁게 두면 안 된다.
		then(refreshTokenRepository).should(never()).findByCurrentTokenOrPreviousToken(any());
	}

	@Test
	@DisplayName("DB 에 없는 토큰은 거절한다")
	void rejectsUnknownToken() {
		given(refreshTokenRepository.findByCurrentTokenOrPreviousToken(PHONE_TOKEN))
			.willReturn(Optional.empty());

		assertThatThrownBy(() -> authService.reissueToken(request(PHONE_TOKEN)))
			.isInstanceOf(CustomException.class);
	}

	@Test
	@DisplayName("직전 토큰으로 10초 안에 다시 오면 회전 없이 현재 토큰을 돌려준다")
	void graceWindowReturnsCurrentWithoutRotating() {
		// 앱은 401 을 받은 요청마다 재발급을 걸어서, 같은 토큰으로 동시에 여러 건이 들어온다.
		// 첫 건이 회전시킨 뒤 나머지가 옛 토큰으로 도착하는데, 이걸 거절하면 멀쩡한 세션이 끊긴다.
		RefreshToken stored = row("already.rotated", PHONE_TOKEN);
		given(refreshTokenRepository.findByCurrentTokenOrPreviousToken(PHONE_TOKEN))
			.willReturn(Optional.of(stored));
		given(jwtUtil.createAccessToken(eq(USER_ID), any())).willReturn("new.access");

		TokenReissueResponse response = authService.reissueToken(request(PHONE_TOKEN));

		assertThat(response.refreshToken()).isEqualTo("already.rotated");
		assertThat(stored.getCurrentToken()).isEqualTo("already.rotated");
		// grace 경로에서는 새 refresh 를 만들지 않는다 — 만들면 회전이 무한히 이어진다.
		then(jwtUtil).should(never()).createRefreshToken(any());
	}

	@Test
	@DisplayName("grace 창을 넘긴 직전 토큰은 회전 대상이다 — 무한 재사용을 막는다")
	void stalePreviousTokenRotatesInsteadOfBeingReused() {
		RefreshToken stored = row("already.rotated", PHONE_TOKEN);
		setUpdatedAt(stored, LocalDateTime.now().minusMinutes(5));
		given(refreshTokenRepository.findByCurrentTokenOrPreviousToken(PHONE_TOKEN))
			.willReturn(Optional.of(stored));
		given(jwtUtil.createAccessToken(eq(USER_ID), any())).willReturn("new.access");
		given(jwtUtil.createRefreshToken(USER_ID)).willReturn("rotated.refresh");

		TokenReissueResponse response = authService.reissueToken(request(PHONE_TOKEN));

		assertThat(response.refreshToken()).isEqualTo("rotated.refresh");
	}

	@Test
	@DisplayName("로그인 직후 10초 안의 재발급은 회전하지 않고 같은 토큰을 돌려준다")
	void reissueRightAfterLoginHitsGraceWindow() {
		// UserServiceV2.socialLogin() 은 RefreshToken.create(userId, token, token) 으로
		// current 와 previous 를 같은 값으로 넣는다. 그래서 로그인 직후 재발급이 들어오면
		// previousToken.equals(요청) 이 성립해 grace 분기를 탄다 — 회전하지 않는다.
		// 의도된 동작은 아니지만 해롭지도 않아서(10초 지나면 정상 회전) 현재 동작을 고정해 둔다.
		// create() 의 인자를 바꾸면 여기서 깨진다.
		RefreshToken justLoggedIn = row(PHONE_TOKEN, PHONE_TOKEN);
		given(refreshTokenRepository.findByCurrentTokenOrPreviousToken(PHONE_TOKEN))
			.willReturn(Optional.of(justLoggedIn));
		given(jwtUtil.createAccessToken(eq(USER_ID), any())).willReturn("new.access");

		TokenReissueResponse response = authService.reissueToken(request(PHONE_TOKEN));

		assertThat(response.refreshToken()).isEqualTo(PHONE_TOKEN);
		then(jwtUtil).should(never()).createRefreshToken(any());
	}

	@Test
	@DisplayName("같은 유저의 기기 두 대는 서로의 세션을 밀어내지 않는다 (user_id 유니크 금지)")
	void twoDevicesOfSameUserAreIndependent() {
		// 로그인 1회 = 행 1개. 폰과 노트북이 각각 자기 행을 갖는다.
		// previous 를 서로 다르게 둬야 grace 분기가 아니라 회전 경로를 탄다
		// (로그인 직후에는 current == previous 인데, 그 성질은 아래 테스트가 따로 고정한다).
		RefreshToken phoneRow = row(PHONE_TOKEN, "phone.older");
		RefreshToken laptopRow = row(LAPTOP_TOKEN, "laptop.older");
		given(refreshTokenRepository.findByCurrentTokenOrPreviousToken(PHONE_TOKEN))
			.willReturn(Optional.of(phoneRow));
		given(refreshTokenRepository.findByCurrentTokenOrPreviousToken(LAPTOP_TOKEN))
			.willReturn(Optional.of(laptopRow));
		given(jwtUtil.createAccessToken(eq(USER_ID), any())).willReturn("new.access");
		given(jwtUtil.createRefreshToken(USER_ID)).willReturn("phone.rotated");

		authService.reissueToken(request(PHONE_TOKEN));

		// 폰이 회전했다고 노트북 세션이 무효화되면 안 된다.
		assertThat(laptopRow.getCurrentToken()).isEqualTo(LAPTOP_TOKEN);

		given(jwtUtil.createRefreshToken(USER_ID)).willReturn("laptop.rotated");
		TokenReissueResponse laptop = authService.reissueToken(request(LAPTOP_TOKEN));

		assertThat(laptop.refreshToken()).isEqualTo("laptop.rotated");
		assertThat(phoneRow.getCurrentToken()).isEqualTo("phone.rotated");
	}
}
