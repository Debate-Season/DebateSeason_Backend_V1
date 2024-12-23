package com.debateseason_backend_v1.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.Profile;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.user.service.request.ProfileRegisterServiceRequest;
import com.debateseason_backend_v1.domain.user.service.request.SocialLoginServiceRequest;
import com.debateseason_backend_v1.domain.user.service.response.AuthResponse;
import com.debateseason_backend_v1.domain.user.service.response.ProfileRegisterResponse;
import com.debateseason_backend_v1.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceV1 {

	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final JwtUtil jwtUtil;

	@Transactional
	public AuthResponse socialLogin(SocialLoginServiceRequest loginRequest) {

		Long userId = userRepository.findBySocialTypeAndExternalId(
				loginRequest.getSocialType(),
				loginRequest.getExternalId()
			)
			.orElseGet(() -> saveUser(loginRequest))
			.getId();

		String accessToken = jwtUtil.createAccessToken(userId);
		String refreshToken = jwtUtil.createRefreshToken(userId);

		boolean isRegistered = profileRepository.existsByUserId(userId);

		return AuthResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.socialType(loginRequest.getSocialType())
			.isRegistered(isRegistered)
			.build();
	}

	@Transactional
	public ProfileRegisterResponse registerProfile(ProfileRegisterServiceRequest request) {

		if (profileRepository.existsByUserId(request.getUserId())) {
			throw new RuntimeException("이미 프로필이 등록되어 있습니다.");
		}

		Profile profile = Profile.builder()
			.userId(request.getUserId())
			.nickname(request.getNickname())
			.imageUrl(request.getImageUrl())
			.community(request.getCommunity())
			.gender(request.getGender())
			.ageRange(request.getAgeRange())
			.build();

		profileRepository.save(profile);

		return ProfileRegisterResponse.builder()
			.userId(profile.getUserId())
			.nickname(profile.getNickname())
			.build();
	}

	private User saveUser(SocialLoginServiceRequest request) {

		User user = User.of(request.getSocialType(), request.getExternalId());

		return userRepository.save(user);
	}

}
