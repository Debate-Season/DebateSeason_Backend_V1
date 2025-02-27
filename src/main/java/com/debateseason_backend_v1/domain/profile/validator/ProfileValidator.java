package com.debateseason_backend_v1.domain.profile.validator;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProfileValidator {

	private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z]{1,8}$");
	private final ProfileRepository profileRepository;

	public void validateForRegister(Long userId, String nickname) {
		validateProfileNotExists(userId);
		validateNickname(nickname);
	}

	public void validateForUpdate(Long userId, String nickname) {
		validateProfileExists(userId);
		validateNicknameFormat(nickname);
	}

	public void validateNickname(String nickname) {
		validateNicknameFormat(nickname);
		validateNicknameDuplicate(nickname);
	}

	private void validateProfileNotExists(Long userId) {
		if (profileRepository.existsByUserId(userId)) {
			throw new CustomException(ErrorCode.ALREADY_EXIST_PROFILE);
		}
	}

	private void validateProfileExists(Long userId) {
		if (!profileRepository.existsByUserId(userId)) {
			throw new CustomException(ErrorCode.NOT_EXIST_USER);
		}
	}

	public void validateNicknameFormat(String nickname) {
		if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
			throw new CustomException(ErrorCode.INVALID_NICKNAME_FORMAT);
		}
	}

	public void validateNicknameDuplicate(String nickname) {
		if (profileRepository.existsByNickname(nickname)) {
			throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
		}
	}
}
