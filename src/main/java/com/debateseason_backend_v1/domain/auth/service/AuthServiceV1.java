package com.debateseason_backend_v1.domain.auth.service;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.auth.service.request.TokenReissueServiceRequest;
import com.debateseason_backend_v1.domain.auth.service.response.TokenReissueResponse;
import com.debateseason_backend_v1.domain.repository.RefreshTokenRepository;
import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;
import com.debateseason_backend_v1.domain.user.domain.UserRole;
import com.debateseason_backend_v1.domain.user.infrastructure.UserJpaRepository;
import com.debateseason_backend_v1.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceV1 {

	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserJpaRepository userJpaRepository;

	@Transactional
	public TokenReissueResponse reissueToken(TokenReissueServiceRequest request) {
		// 2026-08-09 추가. 이전에는 DB 문자열 매칭만 하고 토큰을 파싱조차 하지 않았다.
		// 그래서 REFRESH_EXPIRE_TIME 설정이 아무 효력이 없었고(세션이 사실상 영구 유지),
		// JWT 시크릿을 교체해도 기존 세션을 끊을 수 없었다.
		// DB 조회보다 먼저 둔다 -> 만료·위조 토큰으로 테이블을 긁지 않는다.
		jwtUtil.validateRefreshToken(request.refreshToken());

		RefreshToken refreshToken = refreshTokenRepository.findByCurrentTokenOrPreviousToken(request.refreshToken())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

		if (
			refreshToken.getPreviousToken().equals(request.refreshToken()) &&
				refreshToken.getUpdatedAt().isAfter(LocalDateTime.now().minusSeconds(10))
		) {
			String accessToken = jwtUtil.createAccessToken(refreshToken.getUserId(), resolveRole(refreshToken.getUserId()));
			return TokenReissueResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken.getCurrentToken())
				.build();
		}

		String newAccessToken = jwtUtil.createAccessToken(refreshToken.getUserId(), resolveRole(refreshToken.getUserId()));
		String newRefreshToken = jwtUtil.createRefreshToken(refreshToken.getUserId());

		refreshToken.update(newRefreshToken);

		return TokenReissueResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(newRefreshToken)
			.build();
	}

	// refresh token 에는 role 을 싣지 않으므로 재발급 시점에 조회한다.
	// 덕분에 권한 변경이 다음 재발급에 반영된다.
	private UserRole resolveRole(Long userId) {
		return userJpaRepository.findById(userId)
			.map(user -> user.toModel().getRole())
			.orElse(UserRole.USER);
	}

	public Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			return Long.valueOf(authentication.getName());
		}
		return null;
	}

}
