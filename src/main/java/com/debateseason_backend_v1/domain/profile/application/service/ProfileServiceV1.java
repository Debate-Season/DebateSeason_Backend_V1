package com.debateseason_backend_v1.domain.profile.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.profile.application.service.request.ProfileRegisterServiceRequest;
import com.debateseason_backend_v1.domain.profile.application.service.request.ProfileUpdateServiceRequest;
import com.debateseason_backend_v1.domain.profile.application.service.response.ProfileResponse;
import com.debateseason_backend_v1.domain.profile.domain.CommunityType;
import com.debateseason_backend_v1.domain.profile.domain.Region;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileEntity;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileJpaRepository;
import com.debateseason_backend_v1.domain.profile.validator.ProfileValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceV1 {

	private final ProfileJpaRepository profileRepository;
	private final ProfileValidator profileValidator;

	@Transactional
	public void register(ProfileRegisterServiceRequest request) {

		validateProfileRegistration(request);

		Region residence = Region.of(request.residenceProvince(), request.residenceDistrict());
		Region hometown = Region.of(request.hometownProvince(), request.hometownDistrict());

		ProfileEntity profile = ProfileEntity.builder()
			.userId(request.userId())
			.profileImage(request.profileImage())
			.nickname(request.nickname())
			.gender(request.gender())
			.ageRange(request.ageRange())
			.communityId(request.communityId())
			.residence(residence)
			.hometown(hometown)
			.build();

		profileRepository.save(profile);
	}

	public ProfileResponse getProfileByUserId(Long userId) {

		ProfileEntity profile = profileRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));

		CommunityType communityType = CommunityType.findById(profile.getCommunityId());

		return ProfileResponse.of(profile, communityType);
	}

	@Transactional
	public void update(ProfileUpdateServiceRequest request) {

		ProfileEntity profile = profileRepository.findByUserId(request.userId())
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));

		validateProfileUpdate(request, profile);

		Region residence = Region.of(request.residenceProvince(), request.residenceDistrict());
		Region hometown = Region.of(request.hometownProvince(), request.hometownDistrict());

		profile.update(
			request.profileImage(), request.nickname(), request.communityId(), request.gender(), request.ageRange(),
			residence, hometown
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

	private void validateProfileUpdate(ProfileUpdateServiceRequest request, ProfileEntity profile) {

		if (!profile.getNickname().equals(request.nickname())) {
			profileValidator.validateNicknamePattern(request.nickname());
			profileValidator.validateNicknameExists(request.nickname());
		}

		profileValidator.validateSupportedCommunity(request.communityId());
	}

}
