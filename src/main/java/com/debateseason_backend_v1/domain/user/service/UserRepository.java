package com.debateseason_backend_v1.domain.user.service;

import java.time.LocalDateTime;
import java.util.List;

import com.debateseason_backend_v1.domain.user.domain.User;
import com.debateseason_backend_v1.domain.user.domain.UserId;
import com.debateseason_backend_v1.domain.user.domain.UserStatus;

public interface UserRepository {

	User save(User user);

	User findById(UserId id);

	User findBySocialId(String socialId);

	List<User> findWithdrawnPendingUsers(UserStatus status, LocalDateTime cutoffDate);

	void updateStatus(Long userId, UserStatus status);
}
