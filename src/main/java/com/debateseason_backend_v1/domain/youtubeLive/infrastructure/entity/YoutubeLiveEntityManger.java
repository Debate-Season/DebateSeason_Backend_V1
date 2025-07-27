package com.debateseason_backend_v1.domain.youtubeLive.infrastructure.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLive;

public class YoutubeLiveEntityManger {

	public Map<String, YoutubeLive> toYoutubeLiveMap(List<YoutubeLiveEntity> youtubeLiveEntityList){

		Map<String, YoutubeLive> youtubeLiveMap = new HashMap<>();

		for(YoutubeLiveEntity e : youtubeLiveEntityList) {

			YoutubeLive youtubeLive = e.from(e);
			youtubeLiveMap.put(youtubeLive.getCategory(), youtubeLive);

		}

		return youtubeLiveMap;

	}
}
