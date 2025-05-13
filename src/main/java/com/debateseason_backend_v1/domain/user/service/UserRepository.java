package com.debateseason_backend_v1.domain.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserStatus;
import com.debateseason_backend_v1.domain.user.infrastructure.UserEntity;

public interface UserRepository {

	User save(User user);

	Optional<UserEntity> findById(Long id);

	User findBySocialId(String socialId);

	List<UserEntity> findByIsDeletedTrueAndUpdatedAtBefore(LocalDateTime cutoffDate);

	void updateStatus(Long userId, UserStatus status);
}
