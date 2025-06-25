package com.debateseason_backend_v1.domain.profile.application.service;

import java.util.Optional;

import com.debateseason_backend_v1.domain.profile.domain.Profile;

public interface ProfileRepository {

	void save(Profile profile);

	boolean existsByUserId(Long userId);

	boolean existsByNickname(String nickname);

	Optional<Profile> findByUserId(Long userId);
}
