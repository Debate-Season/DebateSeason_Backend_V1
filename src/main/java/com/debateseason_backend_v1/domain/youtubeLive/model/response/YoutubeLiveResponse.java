package com.debateseason_backend_v1.domain.youtubeLive.model.response;

import java.util.Map;

import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLive;

import lombok.Getter;

// Read-Only
@Getter
public class YoutubeLiveResponse {
	private final Map<String, YoutubeLive> youtubeLives ;

	public YoutubeLiveResponse(Map<String, YoutubeLive> youtubeLives) {
		this.youtubeLives = youtubeLives;
	}
}
