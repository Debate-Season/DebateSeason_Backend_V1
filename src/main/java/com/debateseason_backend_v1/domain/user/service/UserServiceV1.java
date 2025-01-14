package com.debateseason_backend_v1.domain.user.service;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.RefreshTokenRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.user.service.request.SocialLoginServiceRequest;
import com.debateseason_backend_v1.domain.user.service.response.LoginResponse;
import com.debateseason_backend_v1.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceV1 {

	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional
	public LoginResponse socialLogin(SocialLoginServiceRequest request) {

		User user = userRepository.findBySocialTypeAndIdentifier(
				request.socialType(),
				request.identifier()
			)
			.orElseGet(() -> createNewUser(request));

		String newAccessToken = jwtUtil.createAccessToken(user.getId());
		String newRefreshToken = jwtUtil.createRefreshToken(user.getId());

		saveRefreshToken(user, newRefreshToken, jwtUtil.getRefreshTokenExpireTime());

		boolean profileStatus = profileRepository.existsByUserId(user.getId());

		return LoginResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(newRefreshToken)
			.socialType(request.socialType().getDescription())
			.profileStatus(profileStatus)
			.build();
	}

	private User createNewUser(SocialLoginServiceRequest request) {

		User user = User.builder()
			.socialType(request.socialType())
			.externalId(request.identifier())
			.build();

		return userRepository.save(user);
	}

	private void saveRefreshToken(User user, String refresh, Long expiredMs) {

		LocalDateTime expiration = LocalDateTime.now().plusSeconds(expiredMs / 1000);

		RefreshToken refreshToken = RefreshToken.builder()
			.token(refresh)
			.user(user)
			.expirationAt(expiration)
			.build();

		refreshTokenRepository.save(refreshToken);
	}

}
