package com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
class PageInfo {
	private int totalResults;
	private int resultsPerPage;

	// Getters and setters
}
