package com.debateseason_backend_v1.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.CommunityRepository;
import com.debateseason_backend_v1.domain.repository.ProfileCommunityRepository;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.entity.Community;
import com.debateseason_backend_v1.domain.repository.entity.Profile;
import com.debateseason_backend_v1.domain.repository.entity.ProfileCommunity;
import com.debateseason_backend_v1.domain.user.service.request.ProfileRegisterServiceRequest;
import com.debateseason_backend_v1.domain.user.service.request.ProfileUpdateServiceRequest;
import com.debateseason_backend_v1.domain.user.service.response.ProfileResponse;
import com.debateseason_backend_v1.domain.user.validator.CommunityValidator;
import com.debateseason_backend_v1.domain.user.validator.ProfileValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceV1 {

	private final ProfileRepository profileRepository;
	private final CommunityRepository communityRepository;
	private final ProfileCommunityRepository profileCommunityRepository;
	private final ProfileValidator profileValidator;
	private final CommunityValidator communityValidator;

	@Transactional
	public void register(ProfileRegisterServiceRequest request) {

		profileValidator.validateForRegister(request.userId(), request.nickname());
		communityValidator.validate(request.communityId());

		Profile profile = Profile.builder()
			.userId(request.userId())
			.nickname(request.nickname())
			.gender(request.gender())
			.ageRange(request.ageRange())
			.build();

		Profile savedProfile = profileRepository.save(profile);

		ProfileCommunity profileCommunity = ProfileCommunity.builder()
			.profileId(savedProfile.getId())
			.communityId(request.communityId())
			.build();

		profileCommunityRepository.save(profileCommunity);
	}

	public ProfileResponse getProfileByUserId(Long userId) {

		Profile profile = profileRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_USER));

		ProfileCommunity profileCommunity = profileCommunityRepository.findByProfileId(profile.getId())
			.orElseThrow(
				() -> new CustomException(ErrorCode.NOT_FOUND_COMMUNITY_MEMBERSHIP)
			);

		Community community = communityRepository.findById(profileCommunity.getCommunityId())
			.orElseThrow(() -> new CustomException(ErrorCode.ALREADY_EXIST_PROFILE));

		return ProfileResponse.of(profile, community);
	}

	@Transactional
	public void update(ProfileUpdateServiceRequest request) {

		Profile profile = profileRepository.findByUserId(request.userId())
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_USER));

		profileValidator.validateForUpdate(request.userId(), request.nickname());
		communityValidator.validate(request.communityId());

		profile.update(request.nickname(), request.gender(), request.ageRange());

		ProfileCommunity profileCommunity = profileCommunityRepository.getByProfileId(profile.getId());
		profileCommunity.updateCommunity(request.communityId());
	}

	public void checkNickname(String nickname) {

		profileValidator.validateNickname(nickname);
	}

}
