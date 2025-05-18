package com.debateseason_backend_v1.domain.profile.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileJpaRepository extends JpaRepository<ProfileEntity, Long> {

	boolean existsByNickname(String nickname);

	Optional<ProfileEntity> findByUserId(Long userId);
}
