package com.debateseason_backend_v1.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.domain.repository.entity.ProfileCommunity;

public interface ProfileCommunityRepository extends JpaRepository<ProfileCommunity, Long> {

	@Modifying
	@Query("UPDATE ProfileCommunity pc SET pc.communityId = :communityId WHERE pc.profileId = :profileId")
	void updateCommunity(@Param("profileId") Long profileId, @Param("communityId") Long communityId);

}
