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

	@Transactional
	public TokenReissueResponse reissueToken(TokenReissueServiceRequest request) {
		RefreshToken refreshToken = refreshTokenRepository.findByCurrentTokenOrPreviousToken(request.refreshToken())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

		if (
			refreshToken.getPreviousToken().equals(request.refreshToken()) &&
				refreshToken.getUpdatedAt().isAfter(LocalDateTime.now().minusSeconds(10))
		) {
			String accessToken = jwtUtil.createAccessToken(refreshToken.getUserId());
			return TokenReissueResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken.getCurrentToken())
				.build();
		}

		String newAccessToken = jwtUtil.createAccessToken(refreshToken.getUserId());
		String newRefreshToken = jwtUtil.createRefreshToken(refreshToken.getUserId());

		refreshToken.update(newRefreshToken);

		return TokenReissueResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(newRefreshToken)
			.build();
	}

	public Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			return Long.valueOf(authentication.getName());
		}
		return null;
	}

}
