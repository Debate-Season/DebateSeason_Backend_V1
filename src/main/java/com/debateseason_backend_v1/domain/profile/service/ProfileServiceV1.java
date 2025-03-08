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
import com.debateseason_backend_v1.domain.repository.entity.vo.PersonalInfo;
import com.debateseason_backend_v1.domain.repository.entity.vo.Region;

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

		Region residence = Region.of(request.residenceProvince(), request.residenceDistrict());
		Region hometown = Region.of(request.hometownProvince(), request.hometownDistrict());

		PersonalInfo personalInfo = PersonalInfo.of(
			request.nickname(), request.gender(), request.ageRange(), residence, hometown
		);

		Profile profile = Profile.create(
			request.userId(), request.communityId(), request.profileImage(), personalInfo
		);

		profileRepository.save(profile);
	}

	public ProfileResponse getProfileByUserId(Long userId) {

		Profile profile = profileRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));

		CommunityType communityType = CommunityType.findById(profile.getCommunityId());

		return ProfileResponse.of(profile, communityType);
	}

	@Transactional
	public void update(ProfileUpdateServiceRequest request) {

		log.info("gender: {}", request.gender());

		Profile profile = profileRepository.findByUserId(request.userId())
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));

		validateProfileUpdate(request, profile);

		Region residence = Region.of(request.residenceProvince(), request.residenceDistrict());
		Region hometown = Region.of(request.hometownProvince(), request.hometownDistrict());

		PersonalInfo personalInfo = PersonalInfo.of(
			request.nickname(), request.gender(), request.ageRange(), residence, hometown
		);

		profile.update(
			request.communityId(), request.profileImage(), personalInfo
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

		if (!profile.getPersonalInfo().getNickname().equals(request.nickname())) {
			profileValidator.validateNicknamePattern(request.nickname());
			profileValidator.validateNicknameExists(request.nickname());
		}

		profileValidator.validateSupportedCommunity(request.communityId());
	}

}
