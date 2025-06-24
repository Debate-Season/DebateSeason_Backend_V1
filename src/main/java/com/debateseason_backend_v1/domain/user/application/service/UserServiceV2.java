package com.debateseason_backend_v1.domain.user.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.RefreshTokenRepository;
import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;
import com.debateseason_backend_v1.domain.terms.service.TermsServiceV1;
import com.debateseason_backend_v1.domain.user.application.service.request.OidcLoginServiceRequest;
import com.debateseason_backend_v1.domain.user.application.service.response.LoginResponse;
import com.debateseason_backend_v1.domain.user.component.provider.OidcProviderFactory;
import com.debateseason_backend_v1.domain.user.domain.OAuthProvider;
import com.debateseason_backend_v1.domain.user.infrastructure.UserEntity;
import com.debateseason_backend_v1.domain.user.infrastructure.UserJpaRepository;
import com.debateseason_backend_v1.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceV2 {

	private final JwtUtil jwtUtil;
	private final TermsServiceV1 termsService;
	private final UserJpaRepository userRepository;
	private final ProfileRepository profileRepository;
	private final OidcProviderFactory oidcProviderFactory;
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional
	public LoginResponse socialLogin(OidcLoginServiceRequest request) {

		String userIdentifier = oidcProviderFactory.extractUserId(request.OAuthProvider(), request.idToken());

		UserEntity user = userRepository.findByOAuthProviderAndIdentifier(
				request.OAuthProvider(),
				userIdentifier
			)
			.orElseGet(() -> createNewUser(request.OAuthProvider(), userIdentifier));

		if (user.isDeleted()) {
			user.restore();
		}

		String newAccessToken = jwtUtil.createAccessToken(user.getId());
		String newRefreshToken = jwtUtil.createRefreshToken(user.getId());

		RefreshToken refreshToken = RefreshToken.builder()
			.token(newRefreshToken)
			.user(user)
			.build();

		refreshTokenRepository.save(refreshToken);

		boolean profileStatus = profileRepository.existsByUserId(user.getId());

		boolean termsStatus = termsService.hasAgreedToAllRequiredTerms(user.getId());

		return LoginResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(newRefreshToken)
			.socialType(request.OAuthProvider().getDescription())
			.profileStatus(profileStatus)
			.termsStatus(termsStatus)
			.build();
	}

	private UserEntity createNewUser(OAuthProvider OAuthProvider, String userIdentifier) {

		UserEntity user = UserEntity.builder()
			.OAuthProvider(OAuthProvider)
			.externalId(userIdentifier)
			.build();

		return userRepository.save(user);
	}

	private void saveRefreshToken(UserEntity user, String refresh, Long expiredMs) {

		LocalDateTime expiration = LocalDateTime.now().plusSeconds(expiredMs / 1000);

		RefreshToken refreshToken = RefreshToken.builder()
			.token(refresh)
			.user(user)
			.build();

		refreshTokenRepository.save(refreshToken);
	}
}
