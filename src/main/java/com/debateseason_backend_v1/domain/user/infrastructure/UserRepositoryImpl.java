package com.debateseason_backend_v1.domain.user.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.service.UserRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public Long save(User user) {
		return userJpaRepository.save(UserEntity.from(user)).getId();
	}

	@Override
	public Optional<User> findByIdentifier(String identifier) {
		return userJpaRepository.findByIdentifier(identifier).map(UserEntity::toModel);
	}

}
