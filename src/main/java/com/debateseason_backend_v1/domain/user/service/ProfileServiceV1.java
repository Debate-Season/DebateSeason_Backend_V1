package com.debateseason_backend_v1.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.repository.CommunityRepository;
import com.debateseason_backend_v1.domain.repository.ProfileCommunityRepository;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.entity.Community;
import com.debateseason_backend_v1.domain.repository.entity.Profile;
import com.debateseason_backend_v1.domain.repository.entity.ProfileCommunity;
import com.debateseason_backend_v1.domain.user.service.request.ProfileRegisterServiceRequest;
import com.debateseason_backend_v1.domain.user.service.request.ProfileUpdateServiceRequest;
import com.debateseason_backend_v1.domain.user.service.response.NicknameCheckResponse;
import com.debateseason_backend_v1.domain.user.service.response.ProfileResponse;
import com.debateseason_backend_v1.domain.user.validator.CommunityValidator;
import com.debateseason_backend_v1.domain.user.validator.NicknameValidator;

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
	private final NicknameValidator nicknameValidator;
	private final CommunityValidator communityValidator;

	@Transactional
	public void register(ProfileRegisterServiceRequest request) {

		nicknameValidator.validate(request.nickname());
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

	public ProfileResponse getMyProfile(Long userId) {

		Profile profile = profileRepository.findByUserId(userId)
			.orElseThrow(() -> new RuntimeException("프로필이 존재하지 않습니다."));

		ProfileCommunity profileCommunity = profileCommunityRepository.findByProfileId(profile.getId())
			.orElseThrow(() -> new RuntimeException("프로필의 커뮤니티 정보가 없습니다."));

		Community community = communityRepository.findById(profileCommunity.getCommunityId())
			.orElseThrow(() -> new RuntimeException("커뮤니티를 찾을 수 없습니다."));

		return ProfileResponse.of(profile, community);
	}

	@Transactional
	public void update(ProfileUpdateServiceRequest request) {

		Profile profile = profileRepository.findByUserId(request.userId())
			.orElseThrow(() -> new RuntimeException("프로필이 존재하지 않습니다."));

		nicknameValidator.validate(request.nickname());
		communityValidator.validate(request.communityId());

		profile.update(request.nickname(), request.gender(), request.ageRange());

		ProfileCommunity profileCommunity = profileCommunityRepository.getByProfileId(profile.getId());
		profileCommunity.updateCommunity(request.communityId());
	}

	public NicknameCheckResponse existsByNickname(String nickname) {

		nicknameValidator.validate(nickname);

		return NicknameCheckResponse.builder()
			.available(true)
			.build();
	}

}
