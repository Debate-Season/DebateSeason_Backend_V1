package com.debateseason_backend_v1.media.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.repository.entity.Media;
import com.debateseason_backend_v1.domain.repository.MediaRepository;
import com.debateseason_backend_v1.media.model.response.MediaContainer;
import com.debateseason_backend_v1.media.model.response.MediaResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MediaService {

	private final MediaRepository mediaRepository;

	public ApiResult<MediaContainer> fetch(String type, String time){

		List<Media> mediaList = null;

		// 1-1. 최초 요청인 경우 -> 모두 가져오기
		if( type == null && time == null){
			mediaList = mediaRepository.getAllMedia(LocalDateTime.now().toString());
		}
		// 1-2. 모두 가져오기 커서 기반 페이지네이션
		else if(type == null && time != null){
			mediaList = mediaRepository.getAllMedia(time);

		}
		// 2-1. type( youtube, news, community )별 최초 요청
		else if(type!= null && time == null){
			mediaList = mediaRepository.getMediaByType(LocalDateTime.now().toString(),type);
		}
		// 2-2. type별 커서 기반 페이지네이션
		else if(type!= null && time != null){
			mediaList = mediaRepository.getMediaByType(time,type);
		}

		MediaContainer mediaContainer = new MediaContainer();

		List<MediaResponse> mediaResponses = new ArrayList<>();

		for(Media m:mediaList){
			MediaResponse mediaResponse = MediaResponse.builder()
				.id(m.getId())
				.url(m.getUrl())
				.title(m.getTitle())
				.supplier(m.getMedia())
				.outdated(m.getCreatedAt())
				.build();

			mediaResponses.add(mediaResponse);
		}

		mediaContainer.setItems(mediaResponses);


		return ApiResult.<MediaContainer>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("채팅방을 불러왔습니다.")
			.data(mediaContainer)
			.build();






	}


}
