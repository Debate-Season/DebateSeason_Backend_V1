package com.debateseason_backend_v1.domain.media.infrastructure;

import java.util.List;

import com.debateseason_backend_v1.domain.repository.entity.Media;

public interface MediaRepository {

	void save(Media mediaJpaEntity);
	void delete(Media mediaJpaEntity);

	Media findById(Long id);


	// Custom Method

	// 1. size개만큼 최신 속보 가져오기
	List<Media> findBreakingNews(int size);

	// 2. 가장 최신 1개 가져오기
	Media findSingleLatestNews();

	List<Media> getMediaByTimeAndContentTypeUsingCursor(String time,String type,int size);

	List<Media> getAllMediaByTimeUsingCursor(String time,int size);


}
