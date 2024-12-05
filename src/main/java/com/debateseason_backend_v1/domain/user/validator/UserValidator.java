package com.debateseason_backend_v1.domain.user.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.user.dto.RegisterDTO;
import com.debateseason_backend_v1.domain.user.exception.UserException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserValidator {

	private final UserRepository userRepository;

	public void forRegistration(RegisterDTO registerDTO) {
		validateDuplicateUsername(registerDTO.getUsername());
		validatePassword(registerDTO.getPassword());
		validateRole(registerDTO.getRole());

		log.debug("All validations passed for username: {}", registerDTO.getUsername());
	}

	private void validateDuplicateUsername(String username) {
		if (userRepository.existsByUsername(username)) {
			log.warn("Username already exists: {}", username);
			throw new UserException(
				ErrorCode.DUPLICATE_USERNAME,
				String.format("이미 존재하는 사용자명입니다: %s", username)
			);
		}
	}

	private void validatePassword(String password) {
		if (StringUtils.isBlank(password)) {
			log.warn("Empty password attempted");
			throw new UserException(ErrorCode.EMPTY_PASSWORD, "비밀번호는 공백일 수 없습니다.");
		}
	}

	private void validateRole(String role) {
		if (!isValidRole(role)) {
			log.warn("Role not valid: {}", role);
			throw new UserException(
				ErrorCode.INVALID_ROLE,
				"유효하지 않은 사용자 역할입니다. ROLE_USER 또는 ROLE_ADMIN 이어야 합니다."
			);
		}
	}

	private boolean isValidRole(String role) {
		return role != null && (role.equals("ROLE_USER") || role.equals("ROLE_ADMIN"));
	}
}