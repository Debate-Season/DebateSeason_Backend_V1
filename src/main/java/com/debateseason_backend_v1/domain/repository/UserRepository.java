package com.debateseason_backend_v1.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.user.enums.SocialType;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findBySocialTypeAndIdentifier(SocialType socialType, String identifier);

}

