package com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YouTubeSearchResponse {
	private String kind;
	private String etag;
	private String nextPageToken;
	private String regionCode;
	private PageInfo pageInfo;
	private List<Item> items;

	// Getters and setters
}

