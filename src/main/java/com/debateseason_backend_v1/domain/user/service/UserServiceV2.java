package com.debateseason_backend_v1.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.repository.RefreshTokenRepository;
import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;
import com.debateseason_backend_v1.domain.terms.service.TermsServiceV1;
import com.debateseason_backend_v1.domain.user.component.provider.OidcProviderFactory;
import com.debateseason_backend_v1.domain.user.domain.TokenIssuer;
import com.debateseason_backend_v1.domain.user.domain.TokenPair;
import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserId;
import com.debateseason_backend_v1.domain.user.service.request.OidcLoginServiceRequest;
import com.debateseason_backend_v1.domain.user.service.response.LoginResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceV2 {

	private final TokenIssuer tokenIssuer;
	private final TermsServiceV1 termsService;
	private final UserRepository userRepository;
	private final OidcProviderFactory oidcProviderFactory;
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional
	public LoginResponse socialLogin(OidcLoginServiceRequest request) {

		String socialId = oidcProviderFactory.extractUserId(request.socialType(), request.idToken());

		User user = userRepository.findBySocialId(socialId);

		if (user == User.EMPTY) {
			user = User.register(request.toCommand());
			user = userRepository.save(user);
		} else {
			user.login();
			userRepository.save(user);
		}

		TokenPair tokenPair = user.issueTokens(tokenIssuer);

		saveRefreshToken(user.getId(), tokenPair.refreshToken());

		boolean termsStatus = termsService.hasAgreedToAllRequiredTerms(user.getId());

		return LoginResponse.builder()
			.accessToken(tokenPair.accessToken())
			.refreshToken(tokenPair.refreshToken())
			.socialType(request.socialType().getDescription())
			.profileStatus(user.hasProfile())
			.termsStatus(termsStatus)
			.build();
	}

	private void saveRefreshToken(UserId userId, String refresh) {

		RefreshToken refreshToken = RefreshToken.builder()
			.token(refresh)
			.userId(userId.value())
			.build();

		refreshTokenRepository.save(refreshToken);
	}
}
