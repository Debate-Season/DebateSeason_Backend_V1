package com.debateseason_backend_v1.domain.user.validator;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.CommunityRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommunityValidator {

	private final CommunityRepository communityRepository;

	public void validate(Long communityId) {
		if (!communityRepository.existsById(communityId)) {
			throw new CustomException(ErrorCode.NOT_SUPPORTED_COMMUNITY);
		}
	}

}
