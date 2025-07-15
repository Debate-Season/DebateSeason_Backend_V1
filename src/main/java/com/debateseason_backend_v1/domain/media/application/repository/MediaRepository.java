package com.debateseason_backend_v1.domain.media.application.repository;

import java.util.List;


import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.domain.media.infrastructure.entity.MediaEntity;

public interface MediaRepository {

	// 기본적인 CRUD

	// Id기반으로 Media 레코드를 찾는다.
	MediaEntity findById(Long id);


	// 1. 최신 news, youtube, community 커서 기반 5개 가져오기
	List<MediaEntity> getMediaByType(@Param("time") String time,@Param("type") String type);

	// 2. 모든 미디어 커서 기반 5개 가져오기
	List<MediaEntity> getAllMedia(@Param("time") String time);

	// 3. 속보 가져오기
	List<MediaEntity> findTop10BreakingNews();

	// 4. 가장 최신글 1개 조회를 한다.
	MediaEntity findLatestNews();

	// 5. media를 type(news, community)별로 가져오되, time 기반 페이지네이션을 수행한다.
	// cursor = time이 수행한다.
	public List<MediaEntity> findMediaByTypeAndTimeCursor(String type, String time);


}
