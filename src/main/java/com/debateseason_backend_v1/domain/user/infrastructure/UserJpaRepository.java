package com.debateseason_backend_v1.domain.user.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.user.enums.SocialType;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

	List<UserEntity> findByIsDeletedTrueAndUpdatedAtBefore(LocalDateTime cutoffDate);
}