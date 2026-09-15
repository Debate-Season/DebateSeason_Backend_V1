package com.debateseason_backend_v1.domain.youtubeLive.application.repository;


import java.util.List;


import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;
import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.entity.YoutubeLiveEntity;

public interface YoutubeLiveRepository {

	// 1. Live 카테고리별로 가져오기
	YoutubeLiveEntity fetchByCategory(String category);

	// 2. Live 전체 가져오기
	List<YoutubeLiveEntity> findAll();

	// 3. Live 상세보기
	public YoutubeLiveEntity findById(Integer id);

	// 1.유튜브 라이브 저장
	void save(YoutubeLiveDto youtubeLiveDto);

	// 2. category에 맞게 데이터 가져오기
	YoutubeLiveEntity fetch(String category);

	// 3. category에 맞는 데이터 지우기
	//    진행 중인 라이브가 없으면 끝난 방송을 계속 내려보내지 않도록 지운다.
	void deleteByCategory(String category);

}