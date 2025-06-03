package com.debateseason_backend_v1.domain.youtubeLive.model.response;

import java.util.List;
import java.util.Map;

import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLive;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;

import lombok.Getter;
import lombok.Setter;

// Read-Only
@Getter
@Setter
public class YoutubeLiveResponse {
	private List<YoutubeLiveDto> youtubeLives;
}
