package com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Item {
	private String kind;
	private String etag;
	private Id id;
	private Snippet snippet;

	// Getters and setters
}
