package com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {
	private String kind;
	private String etag;
	private Id id;
	private Snippet snippet;

	// Getters and setters
}
