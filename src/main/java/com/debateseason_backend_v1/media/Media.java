package com.debateseason_backend_v1.media;

import java.util.Collections;
import java.util.List;

import com.debateseason_backend_v1.domain.repository.entity.MediaJpaEntity;
import com.debateseason_backend_v1.media.model.response.BreakingNewsResponse;
import com.debateseason_backend_v1.media.model.response.MediaResponse;
import com.debateseason_backend_v1.media.type.MediaType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Media {

	// 1. N개의 미디어 가져오기
	public List<Object> getMultiMedia(List<MediaJpaEntity> multiMediaJpaEntity, MediaType mediaType){

		// 여러 건의 미디어를
		// 1. breakingNew 타입으로 가공할 수도 있고,
		// 2. MediaResponse 타입으로 반환할 수도 있고,
		// 3. 기타 여러 타입으로 가공할 수도 있다.
		if(mediaType==MediaType.Breaking_News){
			return multiMediaJpaEntity.stream().map(
				e ->
					(Object)BreakingNewsResponse.builder()
						.title(e.getTitle())
						.url(e.getUrl())
						.build()
			).toList()
				;
		}
		else if(mediaType==MediaType.Normal_Media) {
			return multiMediaJpaEntity.stream().map(
				e ->
					(Object)MediaResponse.builder()
						.id(e.getId())
						.src(e.getSrc())
						.url(e.getUrl())
						.title(e.getTitle())
						.supplier(e.getMedia())
						.outdated(e.getCreatedAt())
						.build()
			).toList()
				;
		}
		else{
			// MediaController의 파라미터에서 거른다.
			throw new RuntimeException("mediaType "+ mediaType.toString() +"은/는 도대체 뭘까???");
		}

	}

	// 2. 1개의 미디어 가져오기
	public MediaResponse getSingleMedia(MediaJpaEntity MediaJpaEntity){
		// 단 1개의 Media만 반환을 한다.
		return MediaResponse.builder()
			.id(MediaJpaEntity.getId())
			.src(MediaJpaEntity.getSrc())
			.url(MediaJpaEntity.getUrl())
			.title(MediaJpaEntity.getTitle())
			.supplier(MediaJpaEntity.getMedia())
			.outdated(MediaJpaEntity.getCreatedAt())
			.build()
			;
	}

}
