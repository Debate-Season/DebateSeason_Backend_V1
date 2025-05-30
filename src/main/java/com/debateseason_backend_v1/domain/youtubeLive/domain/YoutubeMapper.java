package com.debateseason_backend_v1.domain.youtubeLive.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.YoutubeLiveEntity;

public class YoutubeMapper {


	// 이거는 1개
	public Map<String,YoutubeLive> toDomain(YoutubeLiveEntity jpaEntitie){
		Map<String,YoutubeLive> youtubeLiveContainer = new HashMap<>();


		YoutubeLive youtubeLive = jpaEntitie.from(jpaEntitie);
		youtubeLiveContainer.put(youtubeLive.getCategory(),youtubeLive);


		return youtubeLiveContainer;

	}

	// 이거는 여러개
	public Map<String,YoutubeLive> toDomain(List<YoutubeLiveEntity> jpaEntities){
		Map<String,YoutubeLive> youtubeLiveContainer = new HashMap<>();


		for(YoutubeLiveEntity e : jpaEntities){
			YoutubeLive youtubeLive = e.from(e);
			youtubeLiveContainer.put(youtubeLive.getCategory(),youtubeLive);
		}

		return youtubeLiveContainer;

	}
}
