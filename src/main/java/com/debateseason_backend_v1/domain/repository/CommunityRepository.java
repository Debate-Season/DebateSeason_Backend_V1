package com.debateseason_backend_v1.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.repository.entity.Community;

public interface CommunityRepository extends JpaRepository<Community, Long> {

	List<Community> findByNameContaining(String name);

	List<Community> findAllByOrderByNameAsc();

}
