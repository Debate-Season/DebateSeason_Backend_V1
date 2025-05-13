package com.debateseason_backend_v1.domain.user.infrastructure;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserId;
import com.debateseason_backend_v1.domain.user.domain.UserStatus;
import com.debateseason_backend_v1.domain.user.service.UserRepository;

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
	public User findById(UserId id) {
		return userJpaRepository.findById(id.value()).map(UserEntity::toModel).orElse(User.EMPTY);
	}

	@Override
	public User findBySocialId(String socialId) {
		return userJpaRepository.findBySocialId(socialId).map(UserEntity::toModel).orElse(User.EMPTY);
	}

	@Override
	public List<User> findWithdrawnPendingUsers(UserStatus status, LocalDateTime cutoffDate) {
		return userJpaRepository.findWithdrawnPendingUsers(status, cutoffDate)
			.stream()
			.map(UserEntity::toModel)
			.toList();
	}

	@Override
	public void updateStatus(Long userId, UserStatus status) {
		userJpaRepository.updateStatus(userId, status);
	}
}
