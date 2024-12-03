package com.debateseason_backend_v1.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.user.dto.RegisterDTO;
import com.debateseason_backend_v1.domain.user.exception.UserException;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class UserServiceV1Test {

	@Autowired
	private UserServiceV1 userServiceV1;

	@Test
	@DisplayName("올바른 정보로 회원가입하면 성공한다")
	void registerUser() {
		// given
		RegisterDTO registerDTO = RegisterDTO.builder()
			.username("tester")
			.password("1234")
			.role("ROLE_USER")
			.build();

		// when
		Long registeredId = userServiceV1.register(registerDTO);

		// then
		assertThat(registeredId).isNotNull();
	}

	@Test
	@DisplayName("중복된 사용자명으로 가입 시도하면 DUPLICATE_USERNAME 예외가 발생한다")
	void duplicateUsername() {
		// given
		RegisterDTO registerDTO = RegisterDTO.builder()
			.username("tester")
			.password("1234")
			.role("ROLE_USER")
			.build();

		userServiceV1.register(registerDTO);

		// when&ten
		UserException exception = assertThrows(UserException.class,
			() -> userServiceV1.register(registerDTO));
		assertThat(exception.getCodeInterface()).isEqualTo(ErrorCode.DUPLICATE_USERNAME);
	}

	@Test
	@DisplayName("빈 비밀번호로 가입 시도하면 EMPTY_PASSWORD 예외가 발생한다")
	void emptyPassword() {
		// given
		RegisterDTO registerDTO = RegisterDTO.builder()
			.username("tester")
			.password("  ")
			.role("ROLE_USER")
			.build();

		// when & then
		UserException exception = assertThrows(UserException.class,
			() -> userServiceV1.register(registerDTO));
		assertThat(exception.getCodeInterface()).isEqualTo(ErrorCode.EMPTY_PASSWORD);
	}

	@Test
	@DisplayName("잘못된 역할로 가입 시도하면 INVALID_ROLE 예외가 발생한다")
	void invalidRole() {
		// given
		RegisterDTO registerDTO = RegisterDTO.builder()
			.username("tester")
			.password("1234")
			.role("INVALID_ROLE")
			.build();

		// when & then
		UserException exception = assertThrows(UserException.class,
			() -> userServiceV1.register(registerDTO));
		assertThat(exception.getCodeInterface()).isEqualTo(ErrorCode.INVALID_ROLE);
	}
}