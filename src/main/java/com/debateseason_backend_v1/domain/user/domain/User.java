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
		this.socialAuthInfo = new SocialAuthInfo(socialId, socialType);
		this.status = UserStatus.PENDING;
		return this;
	}

	public User login() {
		if (!this.status.isAccessible()) {
			throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
		}

		if (this.status == UserStatus.WITHDRAWN_PENDING) {
			this.status = UserStatus.ACTIVE;
		}

		return this;
	}

	public User withdraw() {
		this.status = UserStatus.WITHDRAWN_PENDING;
		return this;
	}

	public boolean hasProfile() {
		return this.status != UserStatus.PENDING;
	}

	public TokenPair issueTokens(TokenIssuer tokenIssuer) {
		return tokenIssuer.issueTokenPair(this.id);
	}
	
}
