package com.debateseason_backend_v1.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.repository.entity.Profile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

	boolean existsByUserId(Long userId);

	boolean existsByNickname(String nickname);

	Optional<Profile> findByUserId(Long userId);
}
