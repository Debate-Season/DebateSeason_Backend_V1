package com.debateseason_backend_v1.domain.youtubeLive.domain;

import java.util.ArrayList;
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
	public List<YoutubeLiveDto> toDomain(List<YoutubeLiveEntity> jpaEntities){
		List<YoutubeLiveDto> youtubeLiveContainer = new ArrayList<>();

		for(YoutubeLiveEntity e : jpaEntities){
			YoutubeLiveDto youtubeLiveDto = YoutubeLiveDto.builder()
				.id(e.getId())
				.title(e.getTitle())
				.supplier(e.getSupplier())
				.videoId(e.getVideoId())
				.category(e.getCategory())
				.createAt(e.getCreatedAt())
				.src(e.getScr())
				.build()
				;

			youtubeLiveContainer.add(youtubeLiveDto);
		}

		return youtubeLiveContainer;

	}
}
