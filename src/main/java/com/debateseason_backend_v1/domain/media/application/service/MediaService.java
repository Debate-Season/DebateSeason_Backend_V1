package com.debateseason_backend_v1.domain.media.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.media.infrastructure.entity.MediaEntityManager;
import com.debateseason_backend_v1.domain.media.application.repository.MediaRepository;
import com.debateseason_backend_v1.domain.media.model.response.MediaContainer;
import com.debateseason_backend_v1.domain.media.infrastructure.entity.MediaEntity;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLive;
import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.entity.YoutubeLiveEntity;
import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.entity.YoutubeLiveEntityManger;
import com.debateseason_backend_v1.domain.youtubeLive.application.repository.YoutubeLiveRepository;
import com.debateseason_backend_v1.domain.media.model.response.MediaResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MediaService {

	private final MediaRepository mediaRepository;
	private final YoutubeLiveRepository youtubeLiveRepository;

	public ApiResult<MediaContainer> fetch(String type, String time){

		// 1. 미디어JpaEntityList
		List<MediaEntity> mediaEntityList = mediaRepository.findMediaByTypeAndTimeCursor(type, time);
		// MediaEntityManager
		MediaEntityManager mediaEntityManager = new MediaEntityManager();
		List<MediaResponse> mediaResponseList = mediaEntityManager.toMediaResponseList(mediaEntityList);


		// 2. 유튜브JpaEntityList
		List<YoutubeLiveEntity> fetchedAllYoutubeLives = youtubeLiveRepository.findAll();
		// YoutubeLiveEntityManger
		YoutubeLiveEntityManger youtubeLiveEntityManger = new YoutubeLiveEntityManger();
		Map<String, YoutubeLive> youtubeLiveMap = youtubeLiveEntityManger.toYoutubeLiveMap(fetchedAllYoutubeLives);


		MediaContainer mediaContainer = MediaContainer.builder()
			.youtubeLiveContainer(youtubeLiveMap)
			.items(mediaResponseList)
			.build()
			;

		return ApiResult.<MediaContainer>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("채팅방을 불러왔습니다.")
			.data(mediaContainer)
			.build();

	}

	// Dirty-checking
	@Transactional
	public ApiResult<Object> updateMediaViewCount(Long id){
		MediaEntity mediaEntity = mediaRepository.findById(id);

		int count = mediaEntity.getCount()+1;

		mediaEntity.setCount(count);

		return ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("미디어 조회수를 업데이트 했습니다.")
			.build();
	}

}
