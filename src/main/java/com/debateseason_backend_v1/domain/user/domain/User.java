package com.debateseason_backend_v1.domain.user.domain;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;

import lombok.Builder;

public class User {

	public static final User EMPTY = new User(UserId.EMPTY, null, null, null);

	private UserId id;
	private String providerId;
	private OAuthProvider OAuthProvider;
	private UserStatus status;

	@Builder
	private User(UserId id, String providerId, OAuthProvider OAuthProvider, UserStatus status) {
		this.id = id;
		this.providerId = providerId;
		this.OAuthProvider = OAuthProvider;
		this.status = status;
	}

	public static User create(String providerId, OAuthProvider OAuthProvider) {
		return User.builder()
			.id(UserId.EMPTY)
			.providerId(providerId)
			.OAuthProvider(OAuthProvider)
			.status(UserStatus.ACTIVE)
			.build();
	}

	public void login() {
		if (!status.canLogin()) {
			throw new CustomException(ErrorCode.NOT_LOGINABLE);
		}

		if (status == UserStatus.WITHDRAWAL_PENDING) {
			status = UserStatus.ACTIVE;
		}
	}

	public void withdraw() {
		if (!status.canWithdrawalRequest()) {
			throw new CustomException(ErrorCode.NOT_WITHDRAWABLE);
		}

		status = UserStatus.WITHDRAWAL_PENDING;
	}

	public void anonymize(String uuid) {
		if (!status.canAnonymize()) {
			throw new CustomException(ErrorCode.NOT_ANONYMIZABLE);
		}

		providerId = uuid;
		status = UserStatus.WITHDRAWAL;
	}

}
