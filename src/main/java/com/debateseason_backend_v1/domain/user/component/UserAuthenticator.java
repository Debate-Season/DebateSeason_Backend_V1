package com.debateseason_backend_v1.domain.user.component;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.user.domain.OidcUserInfo;
import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.policy.login.LoginPolicy;
import com.debateseason_backend_v1.domain.user.service.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserAuthenticator {

	private final UserRepository userRepository;
	private final LoginPolicy loginPolicy;

	public User authenticate(OidcUserInfo info) {

		return userRepository.findByIdentifier(info.identifier())
			.map(u -> {
				loginPolicy.check(u);
				return u;
			})
			.orElseGet(() -> {
				return User.create(info);
			});
	}

}