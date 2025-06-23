package com.debateseason_backend_v1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class YouTubeConfig {

	@Value("${youtube_live-api-key}")
	private String key;

}
