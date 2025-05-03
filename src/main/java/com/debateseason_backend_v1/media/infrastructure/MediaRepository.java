package com.debateseason_backend_v1.media.infrastructure;

import java.util.List;

import com.debateseason_backend_v1.domain.repository.entity.MediaJpaEntity;

public interface MediaRepository {

	void save(MediaJpaEntity mediaJpaEntity);
	void delete(MediaJpaEntity mediaJpaEntity);

	MediaJpaEntity findById(Long id);


	// Custom Method

	// 1. size개만큼 최신 속보 가져오기
	List<MediaJpaEntity> findBreakingNews(int size);

	// 2. 가장 최신 1개 가져오기
	MediaJpaEntity findSingleLatestNews();

	List<MediaJpaEntity> getMediaByTimeAndContentTypeUsingCursor(String time,String type,int size);

	List<MediaJpaEntity> getAllMediaByTimeUsingCursor(String time,int size);


}
