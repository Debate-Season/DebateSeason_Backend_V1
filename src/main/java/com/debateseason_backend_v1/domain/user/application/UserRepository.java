package com.debateseason_backend_v1.domain.user.application;

import java.util.List;
import java.util.Optional;

import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserStatus;

public interface UserRepository {

	User save(User user);

	Optional<User> findById(Long id);

	User findByIdentifier(String providerId);

	List<User> findByStatus(UserStatus status);
}
