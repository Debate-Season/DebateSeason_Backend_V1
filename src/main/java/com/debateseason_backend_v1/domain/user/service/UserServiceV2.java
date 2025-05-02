package com.debateseason_backend_v1.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.terms.service.TermsServiceV1;
import com.debateseason_backend_v1.domain.user.component.UserAuthenticator;
import com.debateseason_backend_v1.domain.user.component.provider.OidcProviderFactory;
import com.debateseason_backend_v1.domain.user.controller.response.LoginResponse;
import com.debateseason_backend_v1.domain.user.domain.OidcUserInfo;
import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.service.request.OidcLoginServiceRequest;
import com.debateseason_backend_v1.security.jwt.TokenIssuer;
import com.debateseason_backend_v1.security.jwt.Tokens;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceV2 {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;

	private final OidcProviderFactory oidcProviderFactory;
	final UserAuthenticator userAuthenticator;
	private final TokenIssuer tokenIssuer;
	private final TermsServiceV1 termsService;

	@Transactional
	public LoginResponse socialLogin(OidcLoginServiceRequest request) {

		OidcUserInfo oidcUserInfo = oidcProviderFactory.extractUserId(request.socialType(), request.idToken());

		User user = userAuthenticator.authenticate(oidcUserInfo);

		userRepository.save(user);

		Tokens tokens = tokenIssuer.issue(user.getId());
		refreshTokenRepository.save(tokens.refreshToken());

		// TODO: 다음 리팩토링
		boolean termsStatus = termsService.hasAgreedToAllRequiredTerms(user.getId().value());

		return LoginResponse.builder()
			.accessToken(tokens.accessToken())
			.refreshToken(tokens.refreshToken().getToken())
			.socialType(request.socialType().getDescription())
			.profileStatus(user.hasProfile())
			.termsStatus(termsStatus)
			.build();
	}

}
