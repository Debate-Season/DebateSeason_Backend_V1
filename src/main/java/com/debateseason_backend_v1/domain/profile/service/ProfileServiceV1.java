package com.debateseason_backend_v1.domain.profile.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.profile.service.request.ProfileRegisterServiceRequest;
import com.debateseason_backend_v1.domain.profile.service.request.ProfileUpdateServiceRequest;
import com.debateseason_backend_v1.domain.profile.service.response.ProfileResponse;
import com.debateseason_backend_v1.domain.profile.validator.ProfileValidator;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.entity.Profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceV1 {

	private final ProfileRepository profileRepository;
	private final ProfileValidator profileValidator;

	@Transactional
	public void register(ProfileRegisterServiceRequest request) {

		validateProfileRegistration(request);

		Profile profile = Profile.builder()
			.userId(request.userId())
			.profileColor(request.profileColor())
			.nickname(request.nickname())
			.gender(request.gender())
			.ageRange(request.ageRange())
			.communityId(request.communityId())
			.build();

		profileRepository.save(profile);
	}

	public ProfileResponse getProfileByUserId(Long userId) {

		Profile profile = profileRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_USER));

		CommunityType communityType = CommunityType.findById(profile.getCommunityId());

		return ProfileResponse.of(profile, communityType);
	}

	@Transactional
	public void update(ProfileUpdateServiceRequest request) {

		Profile profile = profileRepository.findByUserId(request.userId())
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));

		validateProfileUpdate(request, profile);

		profile.update(
			request.profileColor(), request.nickname(), request.communityId(), request.gender(), request.ageRange()
		);
	}

	public void checkNicknameAvailability(String nickname) {

		profileValidator.validateNicknamePattern(nickname);
		profileValidator.validateNicknameExists(nickname);
	}

	private void validateProfileRegistration(ProfileRegisterServiceRequest request) {

		profileValidator.validateProfileExists(request.userId());
		profileValidator.validateNicknamePattern(request.nickname());
		profileValidator.validateNicknameExists(request.nickname());
		profileValidator.validateSupportedCommunity(request.communityId());
	}

	private void validateProfileUpdate(ProfileUpdateServiceRequest request, Profile profile) {

		if (!profile.getNickname().equals(request.nickname())) {
			profileValidator.validateNicknamePattern(request.nickname());
			profileValidator.validateNicknameExists(request.nickname());
		}

		profileValidator.validateSupportedCommunity(request.communityId());
	}

}
