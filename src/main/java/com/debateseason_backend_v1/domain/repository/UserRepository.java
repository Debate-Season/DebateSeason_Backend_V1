package com.debateseason_backend_v1.domain.repository;

import com.debateseason_backend_v1.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByName(String name);

}
