package com.debateseason_backend_v1.domain.youtubeLive.infrastructure;


import java.util.List;

import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;

public interface YoutubeLiveRepository {

	// 1. Live 카테고리별로 가져오기
	YoutubeLiveEntity fetchByCategory(String category);

	// 2. Live 전체 가져오기
	List<YoutubeLiveEntity> findAll();
}