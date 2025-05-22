package com.debateseason_backend_v1.domain.repository;




import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.domain.repository.entity.Media;

public interface MediaRepository extends JpaRepository<Media,Long> {

	// 1. 최신 news, youtube, community 커서 기반 5개 가져오기
	@Query(value = "SELECT * FROM media " +
		"WHERE TYPE = :type AND created_at < (:time) " +
		"ORDER BY created_at DESC " +
		"LIMIT 5",
		nativeQuery = true)
	List<Media> getMediaByType(@Param("time") String time,@Param("type") String type);

	// 2. 모든 미디어 커서 기반 5개 가져오기
	@Query(value = "SELECT * FROM media " +
		"WHERE created_at < (:time) " +
		"ORDER BY created_at DESC " +
		"LIMIT 5",
		nativeQuery = true)
	List<Media> getAllMedia(@Param("time") String time);

	// 3. 속보 가져오기
	@Query(value = "SELECT * FROM media ORDER BY created_at DESC, count DESC LIMIT 10", nativeQuery = true)
	List<Media> findTop10BreakingNews();

	// 4. 가장 최신글 1개 조회를 한다.
	@Query(value = "SELECT * FROM media WHERE type = 'youtube' ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
	Media findLatestNews();

}
