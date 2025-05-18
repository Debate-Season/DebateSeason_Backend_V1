package com.debateseason_backend_v1.domain.user.domain.rules.login;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.user.component.SystemTimeProvider;
import com.debateseason_backend_v1.domain.user.domain.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshUserRule implements LoginRule {

	private final SystemTimeProvider timeProvider;

	@Override
	public void check(User user) {
		if (user.isPendingWithdrawal()) {
			LocalDateTime now = timeProvider.now();
			user.restoreFromWithdrawal(now);
		}
	}

}