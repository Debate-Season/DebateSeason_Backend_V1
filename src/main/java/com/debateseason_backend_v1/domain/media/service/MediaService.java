package com.debateseason_backend_v1.domain.media.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;


import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.repository.entity.Media;

import com.debateseason_backend_v1.domain.media.entity.MediaManager;
import com.debateseason_backend_v1.domain.media.infrastructure.MediaRepository;
import com.debateseason_backend_v1.domain.media.type.MediaType;
import com.debateseason_backend_v1.domain.media.model.response.MediaContainer;
import com.debateseason_backend_v1.domain.media.model.response.MediaResponse;

import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLive;
import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.YoutubeLiveEntity;
import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.YoutubeLiveRepository;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MediaService {

	private final MediaRepository mediaRepository;

	private final YoutubeLiveRepository youtubeLiveRepository;

	private final int cursorSize = 5;
	private final int breakingNewsSize = 10;

	public ApiResult<MediaContainer> fetch(String type, String time){

		MediaManager mediaManager = new MediaManager();

		// 1. 속보 가져오기
		List<Media> breakingNewsJpaEntity = mediaRepository.findBreakingNews(breakingNewsSize);// Repository // 10건 가져오기 수정
		List<Object> breakingNews = mediaManager.getMultiMedia(breakingNewsJpaEntity, MediaType.Breaking_News);

		// 2. 유튜브 미디어 최신 1건 가져오기
		// 유튜브 Live
		YoutubeLiveEntity youtubeLiveEntity = youtubeLiveRepository.fetch("news");
		YoutubeLive youtubeLive = youtubeLiveEntity.from(youtubeLiveEntity);
		

		// 3. type, time 파라미터에 맞게 여러 건의 미디어 가져오기
		List<Media> mediaJpaEntityList = getMediaListByTypeAndTime(type,time);// Repository
		List<Object> mediaResponses = mediaManager.getMultiMedia(mediaJpaEntityList,MediaType.Normal_Media);

		// 4. 반환값 가공하기.
		MediaContainer mediaContainer = MediaContainer.builder()
			.breakingNews(breakingNews)
			.youtubeLive(youtubeLive)
			.items(mediaResponses)
			.build()

			;

		return ApiResult.<MediaContainer>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("미디어를 성공적으로 불러왔습니다..")
			.data(mediaContainer)
			.build();
	}

	// Dirty-checking
	// update
	@Transactional
	public ApiResult<Object> updateMediaViewCount(Long id){
		Media mediaJpaEntity = mediaRepository.findById(id);

		int count = mediaJpaEntity.getCount()+1;

		mediaJpaEntity.setCount(count);

		return ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("미디어 조회수를 업데이트 했습니다.")
			.build();
	}

	private List<Media> getMediaListByTypeAndTime(String type,String time){

		List<Media> mediaJpaEntityList = null;

		// 3-1. 최초 요청인 경우 -> 모두 가져오기
		if( type == null && time == null){
			// Legacy
			mediaJpaEntityList = mediaRepository.getAllMediaByTimeUsingCursor(LocalDateTime.now().toString(),cursorSize);
		}
		// 3-2. 모두 가져오기 커서 기반 페이지네이션
		else if(type == null && time != null){
			// Legacy
			mediaJpaEntityList = mediaRepository.getAllMediaByTimeUsingCursor(time,cursorSize);

		}
		// 3-3. type( youtube, news, community )별 최초 요청
		else if(type!= null && time == null){
			// Legacy
			mediaJpaEntityList = mediaRepository.getMediaByTimeAndContentTypeUsingCursor(LocalDateTime.now().toString(),type,cursorSize);
		}
		// 3-4. type별 커서 기반 페이지네이션
		else if(type!= null && time != null){
			// Legacy
			mediaJpaEntityList = mediaRepository.getMediaByTimeAndContentTypeUsingCursor(time,type,cursorSize);
		}

		return mediaJpaEntityList;

	}



}
