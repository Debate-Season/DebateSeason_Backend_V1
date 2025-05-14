package com.debateseason_backend_v1.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.enums.TokenType;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.RefreshTokenRepository;
import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;
import com.debateseason_backend_v1.domain.terms.service.TermsServiceV1;
import com.debateseason_backend_v1.domain.user.domain.TokenIssuer;
import com.debateseason_backend_v1.domain.user.domain.TokenPair;
import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserId;
import com.debateseason_backend_v1.domain.user.service.request.LogoutServiceRequest;
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
	private final TokenIssuer tokenIssuer;
	private final TermsServiceV1 termsService;
	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional
	public LoginResponse socialLogin(SocialLoginServiceRequest request) {

		User user = userRepository.findBySocialId(request.identifier());

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

	@Transactional
	public void logout(LogoutServiceRequest request) {

		if (!refreshTokenRepository.existsByToken(request.refreshToken())) {
			throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		if (jwtUtil.isExpired(request.refreshToken())) {
			throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
		}

		if (jwtUtil.getTokenType(request.refreshToken()) != TokenType.REFRESH) {
			throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		refreshTokenRepository.deleteByToken(request.refreshToken());
	}

	@Transactional
	public void withdraw(UserId id) {

		User user = userRepository.findById(id);

		user.withdraw();

		userRepository.save(user);
		refreshTokenRepository.deleteAllByUserId(user.getId().value());
	}

	private void saveRefreshToken(UserId userId, String refresh) {

		RefreshToken refreshToken = RefreshToken.builder()
			.token(refresh)
			.userId(userId.value())
			.build();

		refreshTokenRepository.save(refreshToken);
	}

}
