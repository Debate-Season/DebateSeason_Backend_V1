package com.debateseason_backend_v1.domain.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileJpaRepository;
import com.debateseason_backend_v1.domain.repository.RefreshTokenRepository;
import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;
import com.debateseason_backend_v1.domain.terms.service.TermsServiceV1;
import com.debateseason_backend_v1.domain.user.application.UserRepository;
import com.debateseason_backend_v1.domain.user.application.service.request.OidcLoginServiceRequest;
import com.debateseason_backend_v1.domain.user.application.service.response.LoginResponse;
import com.debateseason_backend_v1.domain.user.component.provider.OidcProviderFactory;
import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceV2 {

	private final JwtUtil jwtUtil;
	private final TermsServiceV1 termsService;
	private final UserRepository userRepository;
	private final ProfileJpaRepository profileRepository;
	private final OidcProviderFactory oidcProviderFactory;
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional
	public LoginResponse socialLogin(OidcLoginServiceRequest request) {

		String identifier = oidcProviderFactory.extractUserId(request.socialType(), request.idToken());

		User user = userRepository.findByIdentifier(identifier);

		if (user == User.EMPTY) {
			user = User.create(identifier, request.socialType());
		} else {
			user.login();
		}

		user = userRepository.save(user);

		String accessToken = jwtUtil.createAccessToken(user.getId());
		String refreshToken = jwtUtil.createRefreshToken(user.getId());
		RefreshToken token = RefreshToken.create(user.getId(), refreshToken);

		refreshTokenRepository.save(token);

		boolean profileStatus = profileRepository.existsByUserId(user.getId());
		boolean termsStatus = termsService.hasAgreedToAllRequiredTerms(user.getId());

		return LoginResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.socialType(request.socialType().getDescription())
			.profileStatus(profileStatus)
			.termsStatus(termsStatus)
			.build();
	}
}
