package com.debateseason_backend_v1.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

}
