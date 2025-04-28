package com.debateseason_backend_v1.domain.media.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.domain.repository.entity.MediaJpaEntity;
import com.debateseason_backend_v1.media.Media;
import com.debateseason_backend_v1.media.model.response.MediaResponse;
import com.debateseason_backend_v1.media.type.MediaType;

public class MediaV1Test {

	@Test
	@DisplayName("여러건의 미디어를 MediaType에 맞게 가공을 한 후 반환을 한다.(MediaType.Normal_Media)")
	public void demoGetMultiMedia(){

		LocalDateTime now = LocalDateTime.now();
		// given
		Media media = new Media();

		MediaJpaEntity dummyMediaJpaEntity1 = initMediaJpaEntity1(now);
		MediaJpaEntity dummyMediaJpaEntity2 = initMediaJpaEntity2(now);
		MediaType mediaType = MediaType.Normal_Media;


		List<MediaJpaEntity> multiMediaJpaEntity = new ArrayList<>();
		multiMediaJpaEntity.add(dummyMediaJpaEntity1);
		multiMediaJpaEntity.add(dummyMediaJpaEntity2);

		// when
		List<Object> result = media.getMultiMedia(multiMediaJpaEntity,mediaType);

		// then
		// 1. 결과 리스트가 2개여야 한다.
		assertThat(result.size()).isEqualTo(2);

		// 2. 요소 1과 요소 2가 넣은 dummyMediaJpaEntity1, dummyMediaJpaEntity2와 동일해야만 한다.
		// 값 비교

		MediaResponse element1 = (MediaResponse)result.get(0);
		shouldMatchFieldsOfMediaJpaEntity(element1,dummyMediaJpaEntity1);

		MediaResponse element2 = (MediaResponse)result.get(1);
		shouldMatchFieldsOfMediaJpaEntity(element2,dummyMediaJpaEntity2);
	}

	public MediaJpaEntity initMediaJpaEntity1(LocalDateTime now){
		return MediaJpaEntity.builder()
			.id(1L)
			.title("2025-04-28 오후 6:08")
			.url("https:// webpage url")
			.src("https:// images url")
			.category("정치")
			.media("SBS")
			.type("news")
			.count(0)
			.createdAt(now)
			.build()
			;
	}

	public MediaJpaEntity initMediaJpaEntity2(LocalDateTime now){
		return MediaJpaEntity.builder()
			.id(1L)
			.title("2025-04-28 오후 6:13")
			.url("https:// webpage url???")
			.src("https:// images url???")
			.category("경제")
			.media("SBS")
			.type("youtube")
			.count(0)
			.createdAt(now)
			.build()
			;
	}

	public void shouldMatchFieldsOfMediaJpaEntity(MediaResponse element1, MediaJpaEntity dummyMediaJpaEntity){
		assertThat(element1.getId()).isEqualTo(dummyMediaJpaEntity.getId());
		assertThat(element1.getSrc()).isEqualTo(dummyMediaJpaEntity.getSrc());
		assertThat(element1.getUrl()).isEqualTo(dummyMediaJpaEntity.getUrl());
		assertThat(element1.getTitle()).isEqualTo(dummyMediaJpaEntity.getTitle());
		assertThat(element1.getSupplier()).isEqualTo(dummyMediaJpaEntity.getMedia());
		assertThat(element1.getOutdated()).isEqualTo(dummyMediaJpaEntity.getCreatedAt());






	}
}
