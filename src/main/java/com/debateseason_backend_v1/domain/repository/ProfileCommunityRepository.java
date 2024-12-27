package com.debateseason_backend_v1.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.repository.entity.ProfileCommunity;

public interface ProfileCommunityRepository extends JpaRepository<ProfileCommunity, Long> {
}
