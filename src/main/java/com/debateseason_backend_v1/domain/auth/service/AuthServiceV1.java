package com.debateseason_backend_v1.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.auth.dto.SocialLoginRequest;
import com.debateseason_backend_v1.domain.auth.service.response.AuthResponse;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceV1 {

	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final JwtUtil jwtUtil;

	@Transactional
	public AuthResponse processSocialLogin(SocialLoginRequest loginRequest) {

		User user = userRepository.findBySocialTypeAndExternalId(
				loginRequest.socialType(),
				loginRequest.externalId()
			)
			.orElseGet(() -> createUser(loginRequest));

		String accessToken = jwtUtil.createJwt("access", user.getId(), 600000L);
		String refreshToken = jwtUtil.createJwt("refresh", user.getId(), 86400000L);

		boolean isRegistered = profileRepository.existsByUserId(user.getId());

		return AuthResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.socialType(user.getSocialType())
			.isRegistered(isRegistered)
			.build();
	}

	private User createUser(SocialLoginRequest request) {

		User user = User.builder()
			.socialType(request.socialType())
			.externalId(request.externalId())
			.build();

		return userRepository.save(user);
	}

}
