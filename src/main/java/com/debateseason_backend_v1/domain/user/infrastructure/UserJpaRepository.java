package com.debateseason_backend_v1.domain.user.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.domain.user.domain.UserStatus;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findBySocialId(String socialId);

	@Query("SELECT u FROM UserEntity u WHERE u.status = :status and u.createdAt <= :cutoffDate ")
	List<UserEntity> findWithdrawnPendingUsers(
		@Param("status") UserStatus status,
		@Param("cutoffDate") LocalDateTime cutoffDate
	);

}