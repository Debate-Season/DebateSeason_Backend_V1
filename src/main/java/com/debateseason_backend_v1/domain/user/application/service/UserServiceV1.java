package com.debateseason_backend_v1.domain.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.enums.TokenType;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.RefreshTokenRepository;
import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;
import com.debateseason_backend_v1.domain.terms.service.TermsServiceV1;
import com.debateseason_backend_v1.domain.user.application.UserRepository;
import com.debateseason_backend_v1.domain.user.application.service.request.LogoutServiceRequest;
import com.debateseason_backend_v1.domain.user.application.service.request.SocialLoginServiceRequest;
import com.debateseason_backend_v1.domain.user.application.service.response.LoginResponse;
import com.debateseason_backend_v1.domain.user.domain.User;
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

		User user = userRepository.findByIdentifier(request.identifier());

		if (user == User.EMPTY) {
			user = User.create(request.identifier(), request.socialType());
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
	public void withdraw(Long userId) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

		user.withdraw();

		userRepository.save(user);
		refreshTokenRepository.deleteAllByUserId(user.getId());
	}

}
