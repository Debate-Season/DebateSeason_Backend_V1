package com.debateseason_backend_v1.domain.user.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.user.domain.UserStatus;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findByIdentifier(String identifier);

	List<UserEntity> findByStatus(UserStatus status);

}