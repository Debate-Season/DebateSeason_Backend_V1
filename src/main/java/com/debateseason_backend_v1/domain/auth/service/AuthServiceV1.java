package com.debateseason_backend_v1.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.auth.dto.SocialLoginRequest;
import com.debateseason_backend_v1.domain.auth.service.response.AuthResponse;
import com.debateseason_backend_v1.domain.repository.AuthenticationRepository;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceV1 {

	private final UserRepository userRepository;
	private final AuthenticationRepository authenticationRepository;
	private final ProfileRepository profileRepository;

	public AuthResponse processSocialLogin(SocialLoginRequest request) {

		User user = userRepository.findBySocialTypeAndExternalId(
				request.getSocialType(),
				request.getExternalId()
			)
			.orElseGet(() -> create(request));

		boolean isRegistered = profileRepository.existsByUserId(user.getId());

		return AuthResponse.builder()
			.isRegistered(isRegistered)
			.build();
	}

	private User create(SocialLoginRequest request) {

		User user = User.builder()
			.socialType(request.getSocialType())
			.externalId(request.getExternalId())
			.build();

		return userRepository.save(user);
	}

}
