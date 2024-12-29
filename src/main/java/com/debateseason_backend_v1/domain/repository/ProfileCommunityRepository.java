package com.debateseason_backend_v1.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.domain.repository.entity.ProfileCommunity;

public interface ProfileCommunityRepository extends JpaRepository<ProfileCommunity, Long> {

	Optional<ProfileCommunity> findByProfileId(Long profileId);

	@Query("SELECT pc FROM ProfileCommunity pc WHERE pc.profileId = :profileId")
	ProfileCommunity getByProfileId(@Param("profileId") Long profileId);

}
