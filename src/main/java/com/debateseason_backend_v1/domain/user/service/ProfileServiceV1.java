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

	@Transactional
	public void register(ProfileRegisterServiceRequest request) {

		if (profileRepository.existsByUserId(request.userId())) {
			throw new RuntimeException("이미 프로필이 등록된 사용자입니다.");
		}

		if (profileRepository.existsByNickname(request.nickname())) {
			throw new RuntimeException("중복된 닉네임입니다.");
		}

		Community community = communityRepository.findById(request.communityId())
			.orElseThrow(() -> new RuntimeException("존재하지 않는 커뮤니티입니다."));

		Profile profile = Profile.builder()
			.userId(request.userId())
			.nickname(request.nickname())
			.gender(request.gender())
			.ageRange(request.ageRange())
			.build();

		Profile savedProfile = profileRepository.save(profile);

		ProfileCommunity profileCommunity = ProfileCommunity.builder()
			.profileId(savedProfile.getId())
			.communityId(community.getId())
			.build();

		profileCommunityRepository.save(profileCommunity);
	}

	@Transactional
	public void update(ProfileUpdateServiceRequest request) {

		Profile profile = profileRepository.findByUserId(request.userId())
			.orElseThrow(() -> new RuntimeException("프로필이 존재하지 않습니다."));

		validateNickname(profile, request.nickname());
		updateCommunity(profile.getId(), request.communityId());

		profile.update(request.nickname(), request.gender(), request.ageRange());
	}

	private void validateNickname(Profile profile, String newNickname) {
		if (newNickname != null &&
			!newNickname.equals(profile.getNickname()) &&
			profileRepository.existsByNickname(newNickname)
		) {
			throw new RuntimeException("중복된 닉네임입니다.");
		}
	}

	private void updateCommunity(Long profileId, Long newCommunityId) {
		if (newCommunityId != null) {
			Community community = communityRepository.findById(newCommunityId)
				.orElseThrow(() -> new RuntimeException("지원하지 않는 커뮤니티입니다."));

			profileCommunityRepository.updateCommunity(profileId, community.getId());
		}
	}

}
