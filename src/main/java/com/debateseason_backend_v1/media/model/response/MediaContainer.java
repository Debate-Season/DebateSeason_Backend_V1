package com.debateseason_backend_v1.media.model.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaContainer {

	//private List<BreakingNewsResponse> breakingNews;
	private List<Object> breakingNews;

	private MediaResponse mostRecentMedia;

	//private List<MediaResponse> items;
	private List<Object> items;
}
