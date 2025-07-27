package com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Snippet {
	private String publishedAt;
	private String channelId;
	private String title;
	private String description;

	private Thumbnails thumbnails;

	private String channelTitle;
	private String liveBroadcastContent;
	private String publishTime;

	// Getters and setters
}
