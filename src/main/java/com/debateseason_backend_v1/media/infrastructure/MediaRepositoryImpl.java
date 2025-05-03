package com.debateseason_backend_v1.media.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.MediaJpaRepository;
import com.debateseason_backend_v1.domain.repository.entity.MediaJpaEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class MediaRepositoryImpl implements MediaRepository{

	private final MediaJpaRepository mediaJpaRepository;

	// 기본적인 CRUD
	@Override
	public void save(MediaJpaEntity mediaJpaEntity) {
		mediaJpaRepository.save(mediaJpaEntity);
	}

	@Override
	public void delete(MediaJpaEntity mediaJpaEntity) {
		mediaJpaRepository.delete(mediaJpaEntity);
	}

	@Override
	public MediaJpaEntity findById(Long id) {
		return mediaJpaRepository.findById(id)
			.orElseThrow(
				()-> new CustomException(ErrorCode.MEDIA_NOT_FOUND)
			);
	}

	// Custom 메소드

	// 1. 최신 news, youtube, community 커서 기반 size개 가져오기
	public List<MediaJpaEntity> getMediaByTimeAndContentTypeUsingCursor(String time,String type,int size){
		return mediaJpaRepository.getMediaByType(time,type,size);
	}

	// 2. 모든 미디어 커서 기반 size개 가져오기
	public List<MediaJpaEntity> getAllMediaByTimeUsingCursor(String time,int size){
		return mediaJpaRepository.getAllMedia(time,size);
	}

	// 3. 속보 가져오기
	public List<MediaJpaEntity> findBreakingNews(int size){
		return mediaJpaRepository.findBreakingNews(size);
	}

	// 4. 가장 최신글 1개 조회를 한다.
	public MediaJpaEntity findSingleLatestNews(){
		return mediaJpaRepository.findLatestNews();
	}

}
