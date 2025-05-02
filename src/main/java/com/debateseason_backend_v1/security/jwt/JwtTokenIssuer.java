package com.debateseason_backend_v1.security.jwt;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.user.domain.RefreshToken;
import com.debateseason_backend_v1.domain.user.domain.UserId;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenIssuer implements TokenIssuer {

	private final JwtUtil jwtUtil;

	@Override
	public Tokens issue(UserId userId) {

		String access = jwtUtil.createAccessToken(userId.value());

		String refresh = jwtUtil.createRefreshToken(userId.value());

		RefreshToken refreshToken = RefreshToken.builder()
			.id(userId.value())
			.token(refresh)
			.build();

		return new Tokens(access, refreshToken);
	}

}
