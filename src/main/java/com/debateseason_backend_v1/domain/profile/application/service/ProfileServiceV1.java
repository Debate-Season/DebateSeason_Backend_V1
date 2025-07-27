package com.debateseason_backend_v1.domain.profile.application.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.profile.application.service.request.ProfileCreateServiceRequest;
import com.debateseason_backend_v1.domain.profile.application.service.request.ProfileImageRegisterServiceRequest;
import com.debateseason_backend_v1.domain.profile.application.service.request.ProfileUpdateServiceRequest;
import com.debateseason_backend_v1.domain.profile.application.service.response.ProfileResponse;
import com.debateseason_backend_v1.domain.profile.domain.CommunityType;
import com.debateseason_backend_v1.domain.profile.domain.DistrictType;
import com.debateseason_backend_v1.domain.profile.domain.Nickname;
import com.debateseason_backend_v1.domain.profile.domain.Profile;
import com.debateseason_backend_v1.domain.profile.domain.ProvinceType;
import com.debateseason_backend_v1.domain.profile.domain.Region;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceV1 {

	private final ProfileRepository profileRepository;

	@Transactional
	public void create(ProfileCreateServiceRequest request) {

		if (profileRepository.existsByUserId(request.userId())) {
			throw new CustomException(ErrorCode.ALREADY_EXIST_PROFILE);
		}

		if (profileRepository.existsByNickname(request.nickname())) {
			throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
		}

		validateSupportedCommunity(request.communityId());

		Profile profile = Profile.create(
			request.userId(), request.communityId(),
			Nickname.of(request.nickname()), request.gender(), request.ageRange(),
			Region.of(ProvinceType.UNDEFINED, DistrictType.UNDEFINED),
			Region.of(ProvinceType.UNDEFINED, DistrictType.UNDEFINED)
		);

		profileRepository.save(profile);
	}

	@Transactional
	public void createProfileImage(ProfileImageRegisterServiceRequest request) {

		Profile profile = profileRepository.findByUserId(request.userId())
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));

		profile.updateProfileImage(request.profileImage());

		profileRepository.save(profile);
	}

	public ProfileResponse getProfileByUserId(Long userId) {

		Profile profile = profileRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));

		CommunityType communityType = profile.getCommunityType();

		return ProfileResponse.of(profile, communityType);
	}

	@Transactional
	public void update(ProfileUpdateServiceRequest request) {

		Profile profile = profileRepository.findByUserId(request.userId())
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE));

		if(!profile.getNickname().value().equals(request.nickname())){
			if (profileRepository.existsByNickname(request.nickname())) {
				throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
			}
		}

		validateSupportedCommunity(request.communityId());

		Region residence = Region.of(request.residenceProvince(), request.residenceDistrict());
		Region hometown = Region.of(request.hometownProvince(), request.hometownDistrict());

		profile.update(
			request.communityId(), Nickname.of(request.nickname()),
			request.gender(), request.ageRange(),
			residence, hometown
		);

		profileRepository.save(profile);
	}

	public void checkNicknameAvailability(String nickname) {
		if (profileRepository.existsByNickname(Nickname.of(nickname).value())) {
			throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
		}
	}

	private void validateSupportedCommunity(Long communityId) {
		boolean exists = Arrays.stream(CommunityType.values())
			.anyMatch(type -> type.getId().equals(communityId));

		if (!exists) {
			throw new CustomException(ErrorCode.NOT_SUPPORTED_COMMUNITY);
		}
	}
}
