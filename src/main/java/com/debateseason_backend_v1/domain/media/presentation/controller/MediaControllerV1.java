package com.debateseason_backend_v1.domain.media.presentation.controller;

import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.media.docs.MediaControllerV1Docs;
import com.debateseason_backend_v1.domain.media.model.response.MediaContainer;
import com.debateseason_backend_v1.domain.media.application.service.MediaService;
import com.debateseason_backend_v1.domain.media.domain.MediaType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class MediaControllerV1 implements MediaControllerV1Docs {

	private final MediaService mediaService;

	@GetMapping("/home/media")
	public ApiResult<MediaContainer> getMedia(
		@RequestParam(name = "type",required = false) @Nullable MediaType mediaType,
		@RequestParam(name = "time",required = false)String time){

		String type = mediaType == null ? null : mediaType.toString();
		// type
		// all, 또는 type null -> 모두 가져오기
		// news, youtube, community
		return mediaService.fetch(type,time);
	}

	// 조회수 증가.
	@PostMapping("/media/incrementViewCount")
	public ApiResult<Object> updateMediaViewCount(
		@RequestParam(name = "id") Long id
		){
		return mediaService.updateMediaViewCount(id);
	}
}
