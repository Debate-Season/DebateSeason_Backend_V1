package com.debateseason_backend_v1.domain.user.domain;

import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.user.enums.UserStatus;

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

	public UserPersistenceData mapToPersistenceData() {
		return new UserPersistenceData(this.id, this.socialAuthInfo, this.status);
	}

}
