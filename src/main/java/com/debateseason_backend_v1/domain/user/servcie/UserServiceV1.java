package com.debateseason_backend_v1.domain.user.servcie;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.user.dto.RegisterDTO;
import com.debateseason_backend_v1.domain.user.validator.UserValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceV1 {

	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final UserRepository userRepository;
	private final UserValidator userValidator;

	@Transactional
	public Long register(RegisterDTO registerDTO) {
		userValidator.forRegistration(registerDTO);

		User user = User.builder()
			.username(registerDTO.getUsername())
			.password(bCryptPasswordEncoder.encode(registerDTO.getPassword()))
			.role(registerDTO.getRole())
			.community(registerDTO.getCommunity())
			.build();

		return userRepository.save(user).getId();
	}
}