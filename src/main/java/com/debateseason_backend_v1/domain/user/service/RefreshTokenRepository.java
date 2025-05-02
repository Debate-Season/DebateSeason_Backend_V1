package com.debateseason_backend_v1.domain.user.service;

import com.debateseason_backend_v1.domain.user.domain.RefreshToken;

public interface RefreshTokenRepository {

	void save(RefreshToken refreshToken);

}
