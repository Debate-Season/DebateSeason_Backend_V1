package com.debateseason_backend_v1.domain.youtubeLive.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.entity.YoutubeLiveEntity;

@Repository
public interface YoutubeLiveJpaRepository extends JpaRepository<YoutubeLiveEntity,Integer> {

	// 1.category가 일치하는 엔티티 하나를 가져옴
	YoutubeLiveEntity findByCategory(String category);

}
