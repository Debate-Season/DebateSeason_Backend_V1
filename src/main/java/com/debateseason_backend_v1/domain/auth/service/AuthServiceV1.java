package com.debateseason_backend_v1.domain.auth.service;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
		try {

			RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
				.orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

			String newAccessToken = jwtUtil.createAccessToken(refreshToken.getUser().getId());
			String newRefreshToken = jwtUtil.createRefreshToken(refreshToken.getUser().getId());

			refreshToken.updateToken(newAccessToken);

			return TokenReissueResponse.builder()
				.accessToken(newAccessToken)
				.refreshToken(newRefreshToken)
				.build();

		} catch (ObjectOptimisticLockingFailureException e) {
			log.warn("Refresh Token 재발급 중 동시성 충돌 발생: {}", request.refreshToken());
			throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

	}

	public Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			return Long.valueOf(authentication.getName());
		}
		return null;
	}

}
