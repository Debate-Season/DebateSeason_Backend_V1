package com.debateseason_backend_v1.media.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MediaType {

	Breaking_News("breakingNews"),
	Normal_Media("normalMedia");

	private final String mediaType;



}
