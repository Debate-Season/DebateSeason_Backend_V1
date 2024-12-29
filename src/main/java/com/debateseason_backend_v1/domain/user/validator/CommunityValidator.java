package com.debateseason_backend_v1.domain.user.validator;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.repository.CommunityRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommunityValidator {

	private final CommunityRepository communityRepository;

	public void validate(Long communityId) {
		if (!communityRepository.existsById(communityId)) {
			throw new RuntimeException("지원하지 않는 커뮤니티입니다.");
		}
	}

}
