package com.debateseason_backend_v1.domain.media.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.media.infrastructure.entity.MediaEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class MediaRepositoryImpl implements MediaRepository {

	private final MediaJpaRepository mediaJpaRepository;

	@Override
	public List<MediaEntity> getMediaByType(String time, String type) {
		return mediaJpaRepository.getMediaByType(time,type);
	}

	@Override
	public List<MediaEntity> getAllMedia(String time) {
		return mediaJpaRepository.getAllMedia(time);
	}

	@Override
	public List<MediaEntity> findTop10BreakingNews() {
		return mediaJpaRepository.findTop10BreakingNews();
	}

	@Override
	public MediaEntity findLatestNews() {
		return mediaJpaRepository.findLatestNews();
	}

	@Override
	public MediaEntity findById(Long id) {
		return mediaJpaRepository.findById(id)
			.orElseThrow(
				() -> new CustomException(ErrorCode.MEDIA_NOT_FOUND)
			)
			;
	}

	public List<MediaEntity> findMediaByTypeAndTimeCursor(String type, String time){
		// time이 cursor 역할을 수행한다.

		List<MediaEntity> mediaEntityList = null;

		// 1-1. 최초 요청인 경우 -> 모두 가져오기
		if( type == null && time == null){
			mediaEntityList = mediaJpaRepository.getAllMedia(LocalDateTime.now().toString()); // 오늘 기준
		}
		// 1-2. 모두 가져오기 커서 기반 페이지네이션
		else if(type == null && time != null){
			mediaEntityList = mediaJpaRepository.getAllMedia(time);

		}
		// 2-1. type( youtube, news, community )별 최초 요청
		else if(type!= null && time == null){
			mediaEntityList = mediaJpaRepository.getMediaByType(LocalDateTime.now().toString(),type); // 오늘 기준
		}
		// 2-2. type별 커서 기반 페이지네이션
		else if(type!= null && time != null){
			mediaEntityList = mediaJpaRepository.getMediaByType(time,type);
		}

		return mediaEntityList;

	}
}
