package com.debateseason_backend_v1.domain.profile.service;

import com.debateseason_backend_v1.domain.profile.domain.Nickname;
import com.debateseason_backend_v1.domain.profile.domain.Profile;
import com.debateseason_backend_v1.domain.user.domain.UserId;

public interface ProfileRepository {

	void save(Profile profile);

	Profile findByUserId(UserId userId);

	boolean existsByNickname(Nickname nickname);
}
