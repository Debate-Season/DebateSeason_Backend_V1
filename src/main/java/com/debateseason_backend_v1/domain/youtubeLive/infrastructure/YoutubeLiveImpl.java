package com.debateseason_backend_v1.domain.youtubeLive.infrastructure;



import org.springframework.stereotype.Repository;


import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class YoutubeLiveImpl implements YoutubeLiveRepository{

	private final YoutubeLiveJpaRepository youtubeLiveJpaRepository;

	// 1. categrory별로 가져오기
	@Override
	public YoutubeLiveEntity fetch(String category) {
		YoutubeLiveEntity youtubeLiveEntity =youtubeLiveJpaRepository.findByCategory(category);

		// null일 경우는 없지만, null이면 알려야 한다.
		if(youtubeLiveEntity==null){
			throw new RuntimeException("Something goes wrong. There is no category : "+category);

		}
		return youtubeLiveEntity;
	}
}