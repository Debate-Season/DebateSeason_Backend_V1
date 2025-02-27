package com.debateseason_backend_v1.domain.profile.validator;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProfileValidator {

	private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z]{1,8}$");
	private final ProfileRepository profileRepository;

	public void validateProfileExists(Long userId) {

		if (profileRepository.existsByUserId(userId)) {
			throw new CustomException(ErrorCode.ALREADY_EXIST_PROFILE);
		}
	}

	public void validateSupportedCommunity(Long communityId) {
		boolean exists = Arrays.stream(CommunityType.values())
			.anyMatch(type -> type.getId().equals(communityId));

		if (!exists) {
			throw new CustomException(ErrorCode.NOT_SUPPORTED_COMMUNITY);
		}
	}

	public void validateNicknamePattern(String nickname) {

		if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
			throw new CustomException(ErrorCode.INVALID_NICKNAME_PATTERN);
		}
	}

	public void validateNicknameExists(String nickname) {

		if (profileRepository.existsByNickname(nickname)) {
			throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
		}
	}
}
