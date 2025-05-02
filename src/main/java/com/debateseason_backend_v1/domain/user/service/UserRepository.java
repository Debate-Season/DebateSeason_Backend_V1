package com.debateseason_backend_v1.domain.user.service;

import java.util.Optional;

import com.debateseason_backend_v1.domain.user.domain.User;

public interface UserRepository {

	Long save(User user);

	Optional<User> findByIdentifier(String identifier);

}