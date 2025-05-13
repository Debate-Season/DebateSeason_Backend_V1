package com.debateseason_backend_v1.domain.user.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.RefreshTokenRepository;
import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;
import com.debateseason_backend_v1.domain.terms.service.TermsServiceV1;
import com.debateseason_backend_v1.domain.user.component.provider.OidcProviderFactory;
import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.user.infrastructure.UserEntity;
import com.debateseason_backend_v1.domain.user.service.request.OidcLoginServiceRequest;
import com.debateseason_backend_v1.domain.user.service.response.LoginResponse;
import com.debateseason_backend_v1.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceV2 {

	private final JwtUtil jwtUtil;
	private final TermsServiceV1 termsService;
	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final OidcProviderFactory oidcProviderFactory;
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional
	public LoginResponse socialLogin(OidcLoginServiceRequest request) {

		String userIdentifier = oidcProviderFactory.extractUserId(request.socialType(), request.idToken());

		UserEntity user = userRepository.findBySocialTypeAndIdentifier(
				request.socialType(),
				userIdentifier
			)
			.orElseGet(() -> createNewUser(request.socialType(), userIdentifier));

		if (user.isDeleted()) {
			user.restore();
		}

		String newAccessToken = jwtUtil.createAccessToken(user.getId());
		String newRefreshToken = jwtUtil.createRefreshToken(user.getId());

		saveRefreshToken(user, newRefreshToken, jwtUtil.getRefreshTokenExpireTime());

		boolean profileStatus = profileRepository.existsByUserId(user.getId());

		boolean termsStatus = termsService.hasAgreedToAllRequiredTerms(user.getId());

		return LoginResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(newRefreshToken)
			.socialType(request.socialType().getDescription())
			.profileStatus(profileStatus)
			.termsStatus(termsStatus)
			.build();
	}

	private UserEntity createNewUser(SocialType socialType, String userIdentifier) {

		UserEntity user = UserEntity.builder()
			.socialType(socialType)
			.socialId(userIdentifier)
			.build();

		return userRepository.save(user);
	}

	private void saveRefreshToken(UserEntity user, String refresh, Long expiredMs) {

		LocalDateTime expiration = LocalDateTime.now().plusSeconds(expiredMs / 1000);

		RefreshToken refreshToken = RefreshToken.builder()
			.token(refresh)
			.user(user)
			.expirationAt(expiration)
			.build();

		refreshTokenRepository.save(refreshToken);
	}
}
