package com.debateseason_backend_v1.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String token);

	void deleteByToken(String token);

	@Modifying
	@Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId")
	void deleteAllByUserId(Long userId);

	boolean existsByToken(String token);

}
