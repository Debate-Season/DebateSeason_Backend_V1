package com.debateseason_backend_v1.domain.media.infrastructure.entity;

import java.util.ArrayList;
import java.util.List;

import com.debateseason_backend_v1.domain.media.model.response.MediaResponse;

// 얘는 JPA엔티티를 다루는 매니저임.
// 여러개의 JPA엔티티 -> 도메인으로 할 때
// Domain과 jpa를 다루는 Manager객체는 따로 분리함.
// manager가 pojo가 아닐 경우 만약, jpa가 바뀌면, manager도 같이 수정해야 한다.
public class MediaEntityManager {


	// 1. Entity -> MediaResponse
	public MediaResponse toMediaResponse(MediaEntity mediaEntity){ // 1건
		return MediaResponse.builder()
			.id(mediaEntity.getId())
			.url(mediaEntity.getUrl())// href
			.src(mediaEntity.getSrc())// 이미지 url
			.title(mediaEntity.getTitle())
			.supplier(mediaEntity.getMedia())
			.outdated(mediaEntity.getCreatedAt())
			.type(mediaEntity.getType())
			.build();
	}

	public List<MediaResponse> toMediaResponseList(List<MediaEntity> mediaEntityList){ // 여러건

		List<MediaResponse> mediaResponses = new ArrayList<>();

		for(MediaEntity m: mediaEntityList){
			MediaResponse mediaResponse = toMediaResponse(m);
			mediaResponses.add(mediaResponse);
		}

		return mediaResponses;

	}

}
