package com.debateseason_backend_v1.domain.user.domain.rules.login;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.user.domain.User;

@Component
public class BlockedUserRule implements LoginRule {

	public void check(User user) {
		if (user.isBlock()) {
			throw new RuntimeException("차단 계정입니다.");
		}
	}
}
