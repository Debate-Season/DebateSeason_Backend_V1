package com.debateseason_backend_v1.domain.user.domain;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;

import lombok.Getter;

@Getter
public class User {

	public static final User EMPTY = new User(null, null, null, null);

	private UserId id;
	private SocialAuthInfo socialAuthInfo;
	private UserStatus status;

	public User(UserId id, String socialId, SocialType socialType, UserStatus status) {
		this.id = id;
		this.socialAuthInfo = new SocialAuthInfo(socialId, socialType);
		this.status = status;
	}

	public static User register(UserRegisterCommand command) {

		return new User(UserId.EMPTY, command.socialId(), command.socialType(), UserStatus.PENDING);
	}

	public void login() {
		if (this.status.isNotLoginable()) {
			throw new CustomException(ErrorCode.NOT_LOGINABLE);
		}

		if (this.status == UserStatus.WITHDRAW_PENDING) {
			this.status = UserStatus.ACTIVE;
		}
	}

	public User profileCreated() {
		if (this.status.isNotProfileCreatable()) {
			throw new CustomException(ErrorCode.NOT_PROFILE_CREATABLE);
		}
		this.status = UserStatus.ACTIVE;
		return this;
	}

	public void withdraw() {
		if (this.status.isNotWithdrawable()) {
			throw new CustomException(ErrorCode.NOT_WITHDRAWABLE);
		}

		this.status = UserStatus.WITHDRAW_PENDING;
	}

	public User anonymize(String uuid) {
		if (this.status.isNotAnonymizable()) {
			throw new CustomException(ErrorCode.NOT_ANONYMIZABLE);
		}

		this.socialAuthInfo = new SocialAuthInfo(uuid, SocialType.UNDEFINED);
		this.status = UserStatus.WITHDRAW;

		return this;
	}

	public boolean hasProfile() {
		return this.status != UserStatus.PENDING;
	}

	public TokenPair issueTokens(TokenIssuer tokenIssuer) {
		return tokenIssuer.issueTokenPair(this.id);
	}

}
