package com.debateseason_backend_v1.media.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.media.docs.MediaControllerV1Docs;
import com.debateseason_backend_v1.media.model.response.MediaContainer;
import com.debateseason_backend_v1.media.service.MediaService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class MediaControllerV1 implements MediaControllerV1Docs {

	private final MediaService mediaService;

	@GetMapping("/media")
	public ApiResult<MediaContainer> getMedia(
		@RequestParam(name = "type",required = false)String type,
		@RequestParam(name = "time",required = false)String time){

		// type
		// all, 또는 type null -> 모두 가져오기
		// news, youtube, community
		return mediaService.fetch(type,time);

	}
}
