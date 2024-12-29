package com.debateseason_backend_v1.domain.user.validator;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NicknameValidator {

	private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z]{1,8}$");
	private final ProfileRepository profileRepository;

	public void validate(String nickname) {
		
		if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
			throw new RuntimeException("닉네임은 한글 또는 영문으로 8자 이내로 입력해주세요.");
		}

		if (profileRepository.existsByNickname(nickname)) {
			throw new RuntimeException("중복된 닉네임입니다.");
		}
	}

}
