package com.debateseason_backend_v1.domain.user.servcie;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.user.dto.RegisterDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceV1 {

	private final UserRepository userRepository;

	public Long register(RegisterDTO registerDTO) {
		User user = User.builder()
			.username(registerDTO.getUsername())
			.password(registerDTO.getPassword())
			.role(registerDTO.getRole())
			.build();

		return userRepository.save(user).getId();
	}
}
