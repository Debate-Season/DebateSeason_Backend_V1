package com.debateseason_backend_v1.domain.partner.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.partner.entity.PartnerArticleView;
import com.debateseason_backend_v1.domain.partner.entity.PartnerUsageDaily;
import com.debateseason_backend_v1.domain.partner.repository.PartnerArticleViewRepository;
import com.debateseason_backend_v1.domain.partner.repository.PartnerUsageDailyRepository;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 고객사 조회 수와 호출 미터링을 메모리에 모았다가 1분마다 DB 에 더한다.
 * 상세 1회마다 UPDATE 하면 월 200만 번 쓰기가 된다 (PRD §5). 서버가 한 대라 메모리 집계로 충분하다.
 *
 * <p>조회 수는 "현재값" 을 메모리에 들고 응답에 쓴다 — 처음 볼 때 DB 값을 읽어 오고, 이후 증가분만 더한다.
 * 그래서 반영 주기 사이에도 응답 값이 줄지 않는다. 재시작 때 미반영분은 종료 훅에서 반영한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PartnerCounters {

	private record ViewKey(String partner, Long articleId) {
	}

	private record UsageKey(String partner, LocalDate date, String endpoint) {
	}

	private final PartnerArticleViewRepository viewRepository;
	private final PartnerUsageDailyRepository usageRepository;

	private final Map<ViewKey, AtomicLong> currentViews = new ConcurrentHashMap<>();
	private final Map<ViewKey, LongAdder> pendingViews = new ConcurrentHashMap<>();
	private final Map<UsageKey, LongAdder> pendingUsage = new ConcurrentHashMap<>();

	/** 상세 조회 1회. 증가 후 현재값을 돌려준다. */
	public long recordView(String partner, Long articleId) {
		ViewKey key = new ViewKey(partner, articleId);
		views(partner, List.of(articleId));
		pendingViews.computeIfAbsent(key, k -> new LongAdder()).increment();
		return currentViews.get(key).incrementAndGet();
	}

	/** 여러 기사의 현재 조회 수. 처음 보는 기사는 DB 에서 한 번에 읽는다. */
	public Map<Long, Long> views(String partner, Collection<Long> articleIds) {
		List<PartnerArticleView.Key> missing = articleIds.stream()
			.filter(id -> !currentViews.containsKey(new ViewKey(partner, id)))
			.map(id -> new PartnerArticleView.Key(partner, id))
			.toList();
		if (!missing.isEmpty()) {
			Map<Long, Long> stored = new HashMap<>();
			viewRepository.findAllById(missing).forEach(v -> stored.put(v.getId().getArticleId(), v.getViews()));
			missing.forEach(k -> currentViews.putIfAbsent(new ViewKey(partner, k.getArticleId()),
				new AtomicLong(stored.getOrDefault(k.getArticleId(), 0L))));
		}
		Map<Long, Long> result = new HashMap<>();
		articleIds.forEach(id -> result.put(id, currentViews.get(new ViewKey(partner, id)).get()));
		return result;
	}

	public void recordCall(String partner, LocalDate date, String endpoint) {
		pendingUsage.computeIfAbsent(new UsageKey(partner, date, endpoint), k -> new LongAdder()).increment();
	}

	@Scheduled(fixedDelayString = "${partner.counters.flush-millis:60000}")
	@PreDestroy
	@Transactional
	public void flush() {
		int views = 0;
		for (Map.Entry<ViewKey, LongAdder> e : pendingViews.entrySet()) {
			long delta = e.getValue().sumThenReset();
			if (delta == 0) {
				continue;
			}
			PartnerArticleView.Key id = new PartnerArticleView.Key(e.getKey().partner(), e.getKey().articleId());
			PartnerArticleView row = viewRepository.findById(id).orElseGet(() -> new PartnerArticleView(id, 0));
			row.setViews(row.getViews() + delta);
			viewRepository.save(row);
			views++;
		}
		int usage = 0;
		for (Map.Entry<UsageKey, LongAdder> e : pendingUsage.entrySet()) {
			long delta = e.getValue().sumThenReset();
			if (delta == 0) {
				continue;
			}
			PartnerUsageDaily.Key id = new PartnerUsageDaily.Key(e.getKey().partner(), e.getKey().date(), e.getKey().endpoint());
			PartnerUsageDaily row = usageRepository.findById(id).orElseGet(() -> new PartnerUsageDaily(id, 0));
			row.setCalls(row.getCalls() + delta);
			usageRepository.save(row);
			usage++;
		}
		// 지난 날짜 키는 다 비웠으면 지운다 (메모리가 날마다 늘지 않게)
		pendingUsage.entrySet().removeIf(e -> e.getValue().sum() == 0 && e.getKey().date().isBefore(LocalDate.now()));
		if (views + usage > 0) {
			log.debug("고객사 카운터 반영: 조회 수 {}건, 호출 {}건", views, usage);
		}
	}
}
