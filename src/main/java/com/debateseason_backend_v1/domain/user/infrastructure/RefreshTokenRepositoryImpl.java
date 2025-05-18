package com.debateseason_backend_v1.domain.user.infrastructure;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.user.domain.RefreshToken;
import com.debateseason_backend_v1.domain.user.service.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

	private final RefreshTokenJpaRepository refreshTokenJpaRepository;

	@Override
	public void save(RefreshToken refreshToken) {
		refreshTokenJpaRepository.save(RefreshTokenEntity.from(refreshToken));
	}
	
}
