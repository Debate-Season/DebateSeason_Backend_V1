package com.debateseason_backend_v1.domain.profile.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.profile.domain.Nickname;
import com.debateseason_backend_v1.domain.profile.domain.Profile;
import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.profile.service.request.ProfileCreateServiceRequest;
import com.debateseason_backend_v1.domain.profile.service.request.ProfileFetchServiceRequest;
import com.debateseason_backend_v1.domain.profile.service.request.ProfileUpdateServiceRequest;
import com.debateseason_backend_v1.domain.profile.service.response.ProfileResponse;
import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.service.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceV1 {

	private final ProfileRepository profileRepository;
	private final UserRepository userRepository;

	@Transactional
	public void register(ProfileCreateServiceRequest request) {

		User user = userRepository.findById(request.userId());

		if (user == User.EMPTY) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}

		Profile profile = profileRepository.findByUserId(request.userId());

		if (profile == Profile.EMPTY) {
			profile = Profile.create(request.toCommand());
			user.profileCreated();
			profileRepository.save(profile);
			userRepository.save(user);
		} else {
			throw new CustomException(ErrorCode.ALREADY_EXIST_PROFILE);
		}
	}

	public ProfileResponse getProfileByUserId(ProfileFetchServiceRequest request) {

		Profile profile = profileRepository.findByUserId(request.userId());

		if (profile == Profile.EMPTY) {
			throw new CustomException(ErrorCode.NOT_FOUND_PROFILE);
		}

		CommunityType communityType = CommunityType.findById(profile.getCommunityId().value());

		return ProfileResponse.of(profile, communityType);
	}

	@Transactional
	public void update(ProfileUpdateServiceRequest request) {

		Profile profile = profileRepository.findByUserId(request.userId());

		Profile updatedProfile = profile.update(request.toCommand());

		profileRepository.save(updatedProfile);
	}

	public void checkNicknameAvailability(String query) {

		Nickname nickname = new Nickname(query);

		if (profileRepository.existsByNickname(nickname)) {
			throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
		}

	}

}
