package com.debateseason_backend_v1.domain.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.user.infrastructure.UserEntity;

public interface UserRepository {

	UserEntity save(UserEntity userEntity);

	Optional<UserEntity> findById(Long id);

	Optional<UserEntity> findBySocialTypeAndIdentifier(SocialType socialType, String socialId);

	List<UserEntity> findByIsDeletedTrueAndUpdatedAtBefore(LocalDateTime cutoffDate);
}
