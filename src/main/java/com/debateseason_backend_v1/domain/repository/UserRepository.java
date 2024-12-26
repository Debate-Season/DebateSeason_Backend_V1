package com.debateseason_backend_v1.domain.repository;

import java.util.Optional;

import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findBySocialTypeAndExternalId(SocialType socialType, String externalId);

}

