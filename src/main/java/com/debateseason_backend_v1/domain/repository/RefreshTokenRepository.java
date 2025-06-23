package com.debateseason_backend_v1.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;

import jakarta.persistence.LockModeType;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT rt FROM RefreshToken rt WHERE rt.currentToken = :token OR rt.previousToken = :token")
	Optional<RefreshToken> findByCurrentTokenOrPreviousToken(@Param("token") String token);

	void deleteByCurrentToken(String token);

	@Modifying
	@Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
	void deleteAllByUserId(Long userId);

	boolean existsByCurrentToken(String token);

}
