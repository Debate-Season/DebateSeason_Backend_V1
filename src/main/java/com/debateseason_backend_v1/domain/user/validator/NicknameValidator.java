package com.debateseason_backend_v1.domain.user.validator;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NicknameValidator {

	private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z]{1,8}$");
	private final ProfileRepository profileRepository;

	public void validate(String nickname) {

		if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
			throw new CustomException(ErrorCode.INVALID_NICKNAME_FORMAT);
		}

		if (profileRepository.existsByNickname(nickname)) {
			throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
		}
	}

}
