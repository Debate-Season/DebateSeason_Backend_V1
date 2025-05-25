package com.debateseason_backend_v1.domain.media.model.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaContainer {

	//private List<BreakingNewsResponse> breakingNews;
	private List<Object> breakingNews;

	//private MediaResponse mostRecentMedia;

	private YoutubeLive youtubeLive;

	//private List<MediaResponse> items;
	private List<Object> items;
}
