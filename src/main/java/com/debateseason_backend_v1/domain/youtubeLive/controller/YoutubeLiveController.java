package com.debateseason_backend_v1.domain.youtubeLive.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.youtubeLive.model.response.YoutubeLiveDetail;
import com.debateseason_backend_v1.domain.youtubeLive.model.response.YoutubeLiveResponse;
import com.debateseason_backend_v1.domain.youtubeLive.service.YoutubeLiveService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class YoutubeLiveController {

	private final YoutubeLiveService youtubeLiveService;

	@GetMapping("/lives")
	public ApiResult<YoutubeLiveResponse> getAllYoutubeLives(){

		return youtubeLiveService.getAllYoutubeLives();
	}

	@GetMapping("/live/{id}")
	public ApiResult<YoutubeLiveDetail> getYoutubeLiveDetail(
		@PathVariable(name = "id",required = true) Integer id
	){
		return youtubeLiveService.getYoutubeLiveDetail(id);
	}

}
