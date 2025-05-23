package com.debateseason_backend_v1.media.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.repository.entity.Media;
import com.debateseason_backend_v1.domain.repository.MediaRepository;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLive;
import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.YoutubeLiveEntity;
import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.YoutubeLiveRepository;
import com.debateseason_backend_v1.media.model.response.BreakingNewsResponse;
import com.debateseason_backend_v1.media.model.response.MediaContainer;
import com.debateseason_backend_v1.media.model.response.MediaResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MediaService {

	private final MediaRepository mediaRepository;

	private final YoutubeLiveRepository youtubeLiveRepository;

	public ApiResult<MediaContainer> fetch(String type, String time){

		// 속보 가져오기

		List<BreakingNewsResponse> breakingNews = mediaRepository.findTop10BreakingNews().stream()
			.map(
				e->
					BreakingNewsResponse.builder()
						.title(e.getTitle())
						.url(e.getUrl())
						.build()
			).toList()
			;

		// 최신 미디어 1건 가져오기

		// 필요한 필드 변수들
		// src -> 썸네일
		// url -> 링크
		// title -> 제목
		// media -> 공급업체
		// created_at -> 기사 시간
		Media findLatestMedia = mediaRepository.findLatestNews();

		/*
		MediaResponse mostRecentMedia = MediaResponse.builder()
			.id(findLatestMedia.getId())
			.src(findLatestMedia.getSrc())
			.url(findLatestMedia.getUrl())
			.title(findLatestMedia.getTitle())
			.supplier(findLatestMedia.getMedia())
			.outdated(findLatestMedia.getCreatedAt())
			.build()
			;

		 */


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



		List<MediaResponse> mediaResponses = new ArrayList<>();

		for(Media m:mediaList){
			MediaResponse mediaResponse = MediaResponse.builder()
				.id(m.getId())
				.url(m.getUrl())// href
				.src(m.getSrc())// 이미지 url
				.title(m.getTitle())
				.supplier(m.getMedia())
				.outdated(m.getCreatedAt())
				.build();

			mediaResponses.add(mediaResponse);
		}

		// 유튜브 Live
		YoutubeLiveEntity youtubeLiveEntity = youtubeLiveRepository.fetch("news");

		YoutubeLive youtubeLive = youtubeLiveEntity.from(youtubeLiveEntity);

		MediaContainer mediaContainer = MediaContainer.builder()
			.breakingNews(breakingNews)
			//.mostRecentMedia(mostRecentMedia)
			.youtubeLive(youtubeLive)
			.items(mediaResponses)
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
		Media media = mediaRepository.findById(id)
			.orElseThrow(
				()-> new CustomException(ErrorCode.MEDIA_NOT_FOUND)
			);

		int count = media.getCount()+1;

		media.setCount(count);

		return ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("미디어 조회수를 업데이트 했습니다.")
			.build();
	}




}
