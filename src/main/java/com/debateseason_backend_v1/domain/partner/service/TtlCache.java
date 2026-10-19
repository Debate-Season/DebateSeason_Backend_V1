package com.debateseason_backend_v1.domain.partner.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 짧은 TTL 읽기 캐시. 고객사 트래픽(월 1,200만)은 거의 같은 질의의 반복이고 기사는 하루 수십 건만 바뀐다.
 * 크기가 maxEntries 를 넘으면 통째로 비운다 — 키 공간(키워드 × 페이지)이 작아 정교한 축출이 필요 없다.
 */
class TtlCache<K, V> {

	private record Entry<V>(V value, long expiresAt) {
	}

	private final Map<K, Entry<V>> entries = new ConcurrentHashMap<>();
	private final long ttlMillis;
	private final int maxEntries;

	TtlCache(long ttlMillis, int maxEntries) {
		this.ttlMillis = ttlMillis;
		this.maxEntries = maxEntries;
	}

	/** 있으면 캐시값, 없으면 loader 로 읽어 넣는다. loader 가 null 을 주면 넣지 않는다 (없는 것은 캐시하지 않음). */
	V get(K key, Supplier<V> loader) {
		V hit = peek(key);
		if (hit != null) {
			return hit;
		}
		V value = loader.get();
		if (value != null) {
			put(key, value);
		}
		return value;
	}

	V peek(K key) {
		Entry<V> hit = entries.get(key);
		return hit != null && hit.expiresAt() > System.currentTimeMillis() ? hit.value() : null;
	}

	void put(K key, V value) {
		if (entries.size() >= maxEntries) {
			entries.clear();
		}
		entries.put(key, new Entry<>(value, System.currentTimeMillis() + ttlMillis));
	}

	void remove(K key) {
		entries.remove(key);
	}

	void clear() {
		entries.clear();
	}
}
