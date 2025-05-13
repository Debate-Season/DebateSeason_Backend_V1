package com.debateseason_backend_v1.domain.user.domain;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.user.enums.SocialType;

import lombok.Getter;

@Getter
public class User {

	public static final User EMPTY = new User(null, null, null, null);

	private UserId id;
	private SocialAuthInfo socialAuthInfo;
	private UserStatus status;

	public User(Long id, String socialId, SocialType socialType, UserStatus status) {
		this.id = new UserId(id);
		this.socialAuthInfo = new SocialAuthInfo(socialId, socialType);
		this.status = status;
	}

	public User register(String socialId, SocialType socialType) {
		if (this.status.isNotRegistrable()) {
			throw new CustomException(ErrorCode.USER_ALREADY_REGISTERED);
		}

		this.socialAuthInfo = new SocialAuthInfo(socialId, socialType);
		this.status = UserStatus.PENDING;
		
		return this;
	}

	public void login() {
		if (this.status.isNotLoginable()) {
			throw new CustomException(ErrorCode.NOT_LOGINABLE);
		}

		if (this.status == UserStatus.WITHDRAW_PENDING) {
			this.status = UserStatus.ACTIVE;
		}
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

		return this;
	}

	public boolean hasProfile() {
		return this.status != UserStatus.PENDING;
	}

	public TokenPair issueTokens(TokenIssuer tokenIssuer) {
		return tokenIssuer.issueTokenPair(this.id);
	}

}
