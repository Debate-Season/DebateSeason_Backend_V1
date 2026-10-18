package com.debateseason_backend_v1.domain.sitemap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SitemapService {

	static final int DEFAULT_SIZE = 1000;
	static final int MAX_SIZE = 5000;

	private final SitemapRepository sitemapRepository;

	@Transactional(readOnly = true)
	public SitemapEntry.Page<SitemapEntry.Issue> issues(Long cursor, Integer size) {
		int limit = clamp(size);
		// 한 건 더 읽어 다음 페이지 유무를 안다. 그래야 마지막 페이지에서 빈 호출을 한 번 더 하지 않는다.
		List<Object[]> rows = sitemapRepository.issues(cursor == null ? 0 : cursor, limit + 1);

		List<SitemapEntry.Issue> items = rows.stream().limit(limit)
			.map(row -> new SitemapEntry.Issue(((Number)row[0]).longValue(), SitemapRepository.toLocalDateTime(row[1])))
			.toList();
		return new SitemapEntry.Page<>(items, rows.size() > limit ? items.get(items.size() - 1).issueId() : null);
	}

	@Transactional(readOnly = true)
	public SitemapEntry.Page<SitemapEntry.Thread> threads(Long cursor, Integer size) {
		int limit = clamp(size);
		List<Object[]> rows = sitemapRepository.threads(cursor == null ? 0 : cursor, limit + 1);
		List<Object[]> page = rows.stream().limit(limit).toList();

		List<Long> ids = page.stream().map(row -> ((Number)row[0]).longValue()).toList();
		Map<Long, LocalDateTime> lastChat = sitemapRepository.lastChatAt(ids);

		List<SitemapEntry.Thread> items = page.stream()
			.map(row -> {
				Long id = ((Number)row[0]).longValue();
				LocalDateTime createdAt = SitemapRepository.toLocalDateTime(row[1]);
				return new SitemapEntry.Thread(id, lastChat.getOrDefault(id, createdAt));
			})
			.toList();
		return new SitemapEntry.Page<>(items, rows.size() > limit ? items.get(items.size() - 1).threadId() : null);
	}

	private static int clamp(Integer size) {
		if (size == null || size <= 0) {
			return DEFAULT_SIZE;
		}
		return Math.min(size, MAX_SIZE);
	}
}
