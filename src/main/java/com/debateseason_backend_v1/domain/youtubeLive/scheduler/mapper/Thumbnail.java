package com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Thumbnail {
	private String url;
	private int width;
	private int height;

	// Getters and setters
}
