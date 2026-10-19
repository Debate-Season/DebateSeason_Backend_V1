package com.debateseason_backend_v1.domain.partner.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 고객사 뉴스 응답 캐시 (목록 id·기사 내용, 기본 60초). 조회 서비스와 분리해 둔 이유:
 * 아티클을 고치거나 내리는 쪽(ArticleAdminService)이 조회 서비스에 기대지 않고 바로 비울 수 있게.
 */
@Component
public class PartnerNewsCache {

	final TtlCache<String, List<Long>> lists;
	final TtlCache<Long, Object> contents;

	public PartnerNewsCache(@Value("${partner.cache.ttl-millis:60000}") long ttlMillis) {
		this.lists = new TtlCache<>(ttlMillis, 10_000);
		this.contents = new TtlCache<>(ttlMillis, 10_000);
	}

	/** 기사 하나가 바뀌거나 내려갔다. 그 기사 내용과 목록 전체를 비운다 (목록 키는 질의별이라 통째로). */
	public void evict(Long articleId) {
		contents.remove(articleId);
		lists.clear();
	}
}
