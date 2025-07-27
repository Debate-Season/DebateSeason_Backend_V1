package com.debateseason_backend_v1.domain.user.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.user.application.UserRepository;
import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public User save(User user) {
		return userJpaRepository.save(UserEntity.from(user)).toModel();
	}

	@Override
	public Optional<User> findById(Long userId) {
		return userJpaRepository.findById(userId).map(UserEntity::toModel);
	}

	@Override
	public User findByIdentifier(String identifier) {
		return userJpaRepository.findByIdentifier(identifier).map(UserEntity::toModel).orElse(User.EMPTY);
	}

	@Override
	public List<User> findByStatus(UserStatus status) {
		return userJpaRepository.findByStatus(status).stream().map(UserEntity::toModel).toList();
	}

}
