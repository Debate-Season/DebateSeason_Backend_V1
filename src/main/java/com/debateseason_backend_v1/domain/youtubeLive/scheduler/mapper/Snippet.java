package com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
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
