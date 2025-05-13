package com.debateseason_backend_v1.domain.user.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.user.service.UserRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public UserEntity save(UserEntity userEntity) {
		return userJpaRepository.save(userEntity);
	}

	@Override
	public Optional<UserEntity> findById(Long id) {
		return userJpaRepository.findById(id);
	}

	@Override
	public Optional<UserEntity> findBySocialTypeAndIdentifier(SocialType socialType, String identifier) {
		return userJpaRepository.findBySocialTypeAndIdentifier(socialType, identifier);
	}

	@Override
	public List<UserEntity> findByIsDeletedTrueAndUpdatedAtBefore(LocalDateTime cutoffDate) {
		return userJpaRepository.findByIsDeletedTrueAndUpdatedAtBefore(cutoffDate);
	}
}
