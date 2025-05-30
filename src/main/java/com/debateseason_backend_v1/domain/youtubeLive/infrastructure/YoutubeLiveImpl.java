package com.debateseason_backend_v1.domain.youtubeLive.infrastructure;



import java.util.List;

import org.springframework.stereotype.Repository;


import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class YoutubeLiveImpl implements YoutubeLiveRepository{

	private final YoutubeLiveJpaRepository youtubeLiveJpaRepository;

	// 1. categrory별로 가져오기
	@Override
	public YoutubeLiveEntity fetchByCategory(String category) {
		YoutubeLiveEntity youtubeLiveEntity =youtubeLiveJpaRepository.findByCategory(category);

		// null일 경우는 없지만, null이면 알려야 한다.
		if(youtubeLiveEntity==null){
			throw new RuntimeException("Something goes wrong. There is no category : "+category);

		}
		return youtubeLiveEntity;
	}

	// 2. 모든 YoutubeLive 가져오기 -> 수정 충분히 가능 (무한 스크롤 형식으로 될지도?)
	@Override
	public List<YoutubeLiveEntity> findAll() {
		return youtubeLiveJpaRepository.findAll();
	}
	
	// 3. YoutubeLive 상세보기
	public YoutubeLiveEntity findById(Integer id){
		return youtubeLiveJpaRepository.findById(id).orElseThrow(
			()-> new RuntimeException("YoutubeLive 엔티티 id값이 없다. -> 발생가능성 없음.")
		);
	}
}