package com.debateseason_backend_v1.domain.partner.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 아티클을 고치거나 내리면 고객사 응답 캐시가 바로 비워져야 한다 (60초를 기다리지 않게). */
@DisplayName("고객사 뉴스 캐시")
class PartnerNewsCacheTest {

	@Test
	@DisplayName("evict 는 그 기사 내용과 목록 전체를 비우고, 다른 기사 내용은 남긴다")
	void evict() {
		PartnerNewsCache cache = new PartnerNewsCache(60_000);
		cache.contents.put(1L, "기사 1");
		cache.contents.put(2L, "기사 2");
		cache.lists.put("tmoney|L|부산", List.of(1L, 2L));

		cache.evict(1L);

		assertThat(cache.contents.peek(1L)).isNull();
		assertThat(cache.contents.peek(2L)).isEqualTo("기사 2");
		assertThat(cache.lists.peek("tmoney|L|부산")).isNull();
	}

	@Test
	@DisplayName("loader 가 null 을 주면 캐시하지 않는다 (없는 기사가 60초 동안 없다고 굳지 않게)")
	void nullNotCached() {
		TtlCache<Long, String> cache = new TtlCache<>(60_000, 10);

		assertThat(cache.get(1L, () -> null)).isNull();
		assertThat(cache.get(1L, () -> "생김")).isEqualTo("생김");
	}
}
