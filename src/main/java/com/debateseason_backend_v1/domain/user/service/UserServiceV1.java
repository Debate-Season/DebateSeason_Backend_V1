package com.debateseason_backend_v1.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.enums.TokenType;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.RefreshTokenRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.terms.service.TermsServiceV1;
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
	private final TermsServiceV1 termsService;
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

		if (user.isDeleted()) {
			user.restore();
		}

		String newAccessToken = jwtUtil.createAccessToken(user.getId());
		String newRefreshToken = jwtUtil.createRefreshToken(user.getId());

		RefreshToken refreshToken = RefreshToken.builder()
			.currentToken(newRefreshToken)
			.previousToken(newRefreshToken)
			.user(user)
			.build();

		refreshTokenRepository.save(refreshToken);

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

	@Transactional
	public void logout(LogoutServiceRequest request) {

		if (!refreshTokenRepository.existsByCurrentToken(request.refreshToken())) {
			throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		if (jwtUtil.isExpired(request.refreshToken())) {
			throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
		}

		if (jwtUtil.getTokenType(request.refreshToken()) != TokenType.REFRESH) {
			throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		refreshTokenRepository.deleteByCurrentToken(request.refreshToken());
	}

	@Transactional
	public void withdraw(Long userId) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

		user.withdraw();

		refreshTokenRepository.deleteAllByUserId(user.getId());
	}

	private User createNewUser(SocialLoginServiceRequest request) {

		User user = User.builder()
			.socialType(request.socialType())
			.externalId(request.identifier())
			.build();

		return userRepository.save(user);
	}

}
