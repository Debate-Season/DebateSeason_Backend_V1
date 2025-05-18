package com.debateseason_backend_v1.domain.user.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {

	Optional<RefreshTokenEntity> findByToken(String token);

	void deleteByToken(String token);

	@Modifying
	@Query("DELETE FROM RefreshTokenEntity rt WHERE rt.userId = :userId")
	void deleteAllByUserId(Long userId);

	boolean existsByToken(String token);

}
