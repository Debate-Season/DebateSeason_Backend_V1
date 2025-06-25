package com.debateseason_backend_v1.domain.profile.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.profile.application.service.ProfileRepository;
import com.debateseason_backend_v1.domain.profile.domain.Profile;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProfileRepositoryImpl implements ProfileRepository {

	private final ProfileJpaRepository profileJpaRepository;

	@Override
	public void save(Profile profile) {
		profileJpaRepository.save(ProfileEntity.from(profile));
	}

	@Override
	public boolean existsByUserId(Long userId) {
		return profileJpaRepository.existsByUserId(userId);
	}

	@Override
	public boolean existsByNickname(String nickname) {
		return profileJpaRepository.existsByNickname(nickname);
	}

	@Override
	public Optional<Profile> findByUserId(Long userId) {
		return profileJpaRepository.findByUserId(userId).map(ProfileEntity::toModel);
	}
}
