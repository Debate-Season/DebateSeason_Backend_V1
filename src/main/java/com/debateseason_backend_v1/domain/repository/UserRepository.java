package com.debateseason_backend_v1.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.repository.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
