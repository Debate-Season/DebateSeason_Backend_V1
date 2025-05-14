package com.debateseason_backend_v1.domain.profile.infrastructure;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.profile.domain.Nickname;
import com.debateseason_backend_v1.domain.profile.domain.Profile;
import com.debateseason_backend_v1.domain.profile.service.ProfileRepository;
import com.debateseason_backend_v1.domain.user.domain.UserId;

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
	public Profile findByUserId(UserId userId) {
		return profileJpaRepository.findByUserId(userId.value())
			.map(ProfileEntity::toModel)
			.orElse(Profile.EMPTY);
	}

	@Override
	public boolean existsByNickname(Nickname nickname) {
		return profileJpaRepository.existsByNickname(nickname.value());
	}
}
