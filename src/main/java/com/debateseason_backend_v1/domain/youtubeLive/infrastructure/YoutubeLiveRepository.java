package com.debateseason_backend_v1.domain.youtubeLive.infrastructure;


import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;

public interface YoutubeLiveRepository {

	// 라이브 가져오기
	YoutubeLiveEntity fetch(String category);

}