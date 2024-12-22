package com.debateseason_backend_v1.domain.user.service;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.domain.auth.dto.RegisterRequest;
import com.debateseason_backend_v1.domain.auth.service.response.RegisterResponse;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.entity.Profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceV1 {

	private final ProfileRepository profileRepository;

	public RegisterResponse register(Long userId, RegisterRequest request) {

		Profile profile = Profile.builder()
			.userId(userId)
			.imageUrl(request.getImageUrl())
			.nickname(request.getNickname())
			.community(request.getCommunity())
			.gender(request.getGender())
			.ageRange(request.getAgeRange())
			.build();
		profileRepository.save(profile);

		return RegisterResponse.builder()
			.nickname(profile.getNickname())
			.build();
	}
}
