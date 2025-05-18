package com.debateseason_backend_v1.domain.user.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.enums.TokenType;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.terms.service.TermsServiceV1;
import com.debateseason_backend_v1.domain.user.controller.response.LoginResponse;
import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.infrastructure.RefreshTokenEntity;

import com.debateseason_backend_v1.domain.user.infrastructure.UserEntity;
import com.debateseason_backend_v1.domain.user.service.request.LogoutServiceRequest;
import com.debateseason_backend_v1.domain.user.service.request.SocialLoginServiceRequest;

import com.debateseason_backend_v1.security.jwt.TokenIssuer;
import com.debateseason_backend_v1.security.jwt.Tokens;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceV1 {

	private final TermsServiceV1 termsService;
	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final TokenIssuer tokenIssuer;

	@Transactional
	public LoginResponse socialLogin(SocialLoginServiceRequest request) {
		/*
		Optional<User> user = userRepository.findByIdentifier(request.identifier());

		if (user.isEmpty()) {

		}

		Tokens tokens = tokenIssuer.issue(user.getId());
		saveRefreshToken(user.getId().value(), tokens.refreshToken());

		boolean profileStatus = profileRepository.existsByUserId(userEntity.getId());

		boolean termsStatus = termsService.hasAgreedToAllRequiredTerms(userEntity.getId());



		return LoginResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(newRefreshToken)
			.socialType(request.socialType().getDescription())
			.profileStatus(profileStatus)
			.termsStatus(termsStatus)
			.build();

		 */

		return null;
	}

	@Transactional
	public void logout(LogoutServiceRequest request) {
		/*

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

		 */
	}

	@Transactional
	public void withdraw(Long userId) {

		/*
		UserEntity userEntity = userRepository.findById(userId);

		userEntity.withdraw();

		refreshTokenRepository.deleteAllByUserId(userEntity.getId());

		 */

	}

	private UserEntity createNewUser(SocialLoginServiceRequest request) {

		/*
		UserEntity userEntity = UserEntity.builder()
			.socialType(request.socialType())
			.externalId(request.identifier())
			.build();

		return userRepository.save(userEntity);

		 */
		return null;
	}

	private void saveRefreshToken(Long userId, String refresh, Long expiredMs) {

		/*
		LocalDateTime expiration = LocalDateTime.now().plusSeconds(expiredMs / 1000);

		RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
			.token(refresh)
			.userId(userId)
			.expirationAt(expiration)
			.build();

		refreshTokenRepository.save(refreshTokenEntity);

		 */

	}

}
