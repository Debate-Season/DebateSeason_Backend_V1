package com.debateseason_backend_v1.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.auth.service.request.TokenReissueServiceRequest;
import com.debateseason_backend_v1.domain.auth.service.response.TokenReissueResponse;
import com.debateseason_backend_v1.domain.user.infrastructure.RefreshTokenEntity;
import com.debateseason_backend_v1.domain.user.infrastructure.RefreshTokenJpaRepository;
import com.debateseason_backend_v1.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceV1 {

	private final JwtUtil jwtUtil;
	private final RefreshTokenJpaRepository refreshTokenRepository;

	@Transactional
	public TokenReissueResponse reissueToken(TokenReissueServiceRequest request) {

		RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByToken(request.refreshToken())
			.orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

		String newAccessToken = jwtUtil.createAccessToken(refreshTokenEntity.getId());
		String newRefreshToken = jwtUtil.createRefreshToken(refreshTokenEntity.getId());

		refreshTokenRepository.delete(refreshTokenEntity);

		RefreshTokenEntity token = RefreshTokenEntity.builder()
			.token(newRefreshToken)
			.userId(refreshTokenEntity.getUserId())
			.build();
		refreshTokenRepository.save(token);

		return TokenReissueResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(newRefreshToken)
			.build();
	}

}
