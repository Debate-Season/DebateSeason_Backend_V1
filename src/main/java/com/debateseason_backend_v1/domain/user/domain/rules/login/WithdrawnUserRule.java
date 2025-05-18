package com.debateseason_backend_v1.domain.user.domain.rules.login;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.user.domain.User;

@Component
public class WithdrawnUserRule implements LoginRule {

	public void check(User user) {
		if (user.isWithdrawn()) {
			throw new RuntimeException("탈퇴한 계정입니다.");
		}
	}
}