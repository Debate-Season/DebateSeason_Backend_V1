package com.debateseason_backend_v1.domain.user.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.debateseason_backend_v1.domain.user.domain.UserStatus;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findBySocialId(String socialId);

	List<UserEntity> findByIsDeletedTrueAndUpdatedAtBefore(LocalDateTime cutoffDate);

	@Query("UPDATE UserEntity u SET u.status = :status WHERE u.id = :userId")
	void updateStatus(Long userId, UserStatus status);
}