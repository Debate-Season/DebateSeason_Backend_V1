package com.debateseason_backend_v1.domain.partner.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.article.entity.ArticleStatus;
import com.debateseason_backend_v1.domain.article.entity.ToronchulArticle;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleRepository;
import com.debateseason_backend_v1.domain.ontology.service.ChangeLogWriter;
import com.debateseason_backend_v1.domain.partner.model.PartnerNewsResponse;
import com.debateseason_backend_v1.domain.partner.repository.PartnerNewsQuery;
import com.fasterxml.jackson.databind.JsonNode;


/**
 * 고객사(티머니GO) 뉴스 목록·상세. 데이터 레포가 적재·게시한 tmoney 판만 낸다.
 *
 * <p>민감도 제외는 데이터 쪽이 정본이다 — EXCLUDED·SKIPPED_SENSITIVE 기사는 적재·게시 자체를 하지 않는다.
 * 백엔드는 다시 판정하지 않는다 (정치인 "주제" 규칙 등을 두 곳에서 따로 구현하면 어긋난다).
 *
 * <p>읽기는 60초 캐시를 거친다. 조회 수는 캐시와 별개로 매번 현재값을 붙인다({@link PartnerCounters}).
 */
@Service
public class PartnerNewsService {

	// 고객사 이름 → 판. 지금 고객사는 하나다.
	static final Map<String, String> EDITION_BY_PARTNER = Map.of("tmoney-go", "tmoney");

	static final int DEFAULT_SIZE = 20;
	static final int MAX_SIZE = 50;
	static final Duration POPULAR_WINDOW = Duration.ofDays(7);

	private final PartnerNewsQuery query;
	private final ToronchulArticleRepository articleRepository;
	private final PartnerCounters counters;
	private final ChangeLogWriter json;

	private final TtlCache<String, List<Long>> listCache;
	private final TtlCache<Long, Content> contentCache;

	public PartnerNewsService(PartnerNewsQuery query, ToronchulArticleRepository articleRepository, PartnerCounters counters,
		ChangeLogWriter json, @Value("${partner.cache.ttl-millis:60000}") long cacheMillis) {
		this.query = query;
		this.articleRepository = articleRepository;
		this.counters = counters;
		this.json = json;
		this.listCache = new TtlCache<>(cacheMillis, 10_000);
		this.contentCache = new TtlCache<>(cacheMillis, 10_000);
	}

	/** 캐시에 두는 기사 내용 (고객사와 무관한 부분). */
	private record Content(Long id, String edition, String title, String summary, List<String> body, String category,
		List<String> keywords, LocalDateTime publishedAt, String imageUrl) {
	}

	public enum Sort { POPULAR, LATEST }

	@Transactional(readOnly = true)
	public PartnerNewsResponse.Page list(String partner, String keyword, String sort, String cursor, Integer size) {
		String edition = editionOf(partner);
		counters.recordCall(partner, LocalDate.now(), "news.list");
		int limit = size == null || size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
		String trimmed = keyword == null ? "" : keyword.trim();

		List<Long> ids;
		String next;
		if (!trimmed.isEmpty() || "latest".equalsIgnoreCase(sort)) {
			// 최신순 (키워드가 있으면 키워드 일치만). 키워드 결과가 0건이면 빈 목록 — 인기 뉴스로 채우지 않는다
			Cursor c = Cursor.parseLatest(cursor);
			String cacheKey = edition + "|L|" + trimmed + "|" + cursor + "|" + limit;
			ids = listCache.get(cacheKey, () -> trimmed.isEmpty()
				? query.latest(edition, c.at(), c.id(), limit + 1)
				: query.byKeyword(edition, trimmed, c.at(), c.id(), limit + 1));
			List<Long> page = ids.subList(0, Math.min(limit, ids.size()));
			List<Content> contents = contents(page);
			next = ids.size() > limit ? Cursor.latest(contents.get(contents.size() - 1)) : null;
			return new PartnerNewsResponse.Page(items(partner, contents), next);
		}

		// 인기 뉴스: 최근 7일 발행분을 이 고객사 조회 수 × 시간 감쇠로 정렬
		int offset = Cursor.parseOffset(cursor);
		List<Long> candidates = listCache.get(edition + "|P", () ->
			query.publishedSince(edition, LocalDateTime.now().minus(POPULAR_WINDOW)));
		Map<Long, Long> views = counters.views(partner, candidates);
		List<Content> ranked = new ArrayList<>(contents(candidates));
		LocalDateTime now = LocalDateTime.now();
		ranked.sort(Comparator.comparingDouble((Content c) -> -popularity(views.getOrDefault(c.id(), 0L), c.publishedAt(), now))
			.thenComparing(Content::id, Comparator.reverseOrder()));
		List<Content> page = ranked.subList(Math.min(offset, ranked.size()), Math.min(offset + limit, ranked.size()));
		next = offset + limit < ranked.size() ? Cursor.offset(offset + limit) : null;
		return new PartnerNewsResponse.Page(items(partner, page), next);
	}

	@Transactional(readOnly = true)
	public PartnerNewsResponse.Detail detail(String partner, Long newsId) {
		String edition = editionOf(partner);
		counters.recordCall(partner, LocalDate.now(), "news.detail");
		Content c = contentCache.get(newsId, () -> load(newsId));
		if (c == null || !edition.equals(c.edition())) {
			throw new CustomException(ErrorCode.NOT_FOUND_NEWS);
		}
		long viewCount = counters.recordView(partner, newsId);
		return new PartnerNewsResponse.Detail(c.id(), c.title(), c.summary(), c.body(), c.category(), c.keywords(),
			c.publishedAt(), viewCount, c.imageUrl(), true);
	}

	// 조회 수가 같으면 최신이 위. 하루 지나면 점수가 절반쯤 된다 (HN 식 감쇠, 공식은 D8 초안).
	static double popularity(long views, LocalDateTime publishedAt, LocalDateTime now) {
		double hours = Math.max(0, Duration.between(publishedAt, now).toMinutes() / 60.0);
		return (views + 1) / Math.pow(hours + 2, 1.2);
	}

	private String editionOf(String partner) {
		String edition = EDITION_BY_PARTNER.get(partner);
		if (edition == null) {
			throw new CustomException(ErrorCode.INVALID_PARTNER_API_KEY);
		}
		return edition;
	}

	private List<Content> contents(List<Long> ids) {
		List<Long> missing = new ArrayList<>();
		Map<Long, Content> found = new java.util.HashMap<>();
		for (Long id : ids) {
			Content cached = contentCache.peek(id);
			if (cached != null) {
				found.put(id, cached);
			} else {
				missing.add(id);
			}
		}
		if (!missing.isEmpty()) {
			Map<Long, ToronchulArticle> loaded = articleRepository.findAllById(missing).stream()
				.collect(Collectors.toMap(ToronchulArticle::getId, Function.identity()));
			for (Long id : missing) {
				ToronchulArticle a = loaded.get(id);
				if (a != null && a.getStatus() == ArticleStatus.PUBLISHED) {
					Content c = toContent(a);
					contentCache.put(id, c);
					found.put(id, c);
				}
			}
		}
		return ids.stream().map(found::get).filter(java.util.Objects::nonNull).toList();
	}

	private Content load(Long id) {
		return articleRepository.findById(id)
			.filter(a -> a.getStatus() == ArticleStatus.PUBLISHED)
			.map(this::toContent)
			.orElse(null);
	}

	private Content toContent(ToronchulArticle a) {
		List<String> body = new ArrayList<>();
		for (JsonNode p : json.read(a.getBody())) {
			String text = p.path("text").asText(null);
			if (text != null && !text.isBlank()) {
				body.add(text);
			}
		}
		List<String> keywords = new ArrayList<>();
		if (a.getKeywords() != null) {
			json.read(a.getKeywords()).forEach(k -> keywords.add(k.asText()));
		}
		return new Content(a.getId(), a.getEdition(), a.getHeadline(), a.getSummary(), List.copyOf(body),
			a.getArticleSection(), List.copyOf(keywords), a.getDatePublished(), a.getImageUrl());
	}

	private List<PartnerNewsResponse.Item> items(String partner, List<Content> contents) {
		Map<Long, Long> views = counters.views(partner, contents.stream().map(Content::id).toList());
		return contents.stream().map(c -> new PartnerNewsResponse.Item(c.id(), c.title(), c.summary(), c.category(),
			c.keywords(), c.publishedAt(), views.getOrDefault(c.id(), 0L), c.imageUrl(), true)).toList();
	}

	/** 불투명 커서. 최신순 "L{epochSecond}_{nanos}_{id}", 인기순 "P{offset}". */
	record Cursor(LocalDateTime at, Long id) {

		static String latest(Content c) {
			LocalDateTime t = c.publishedAt();
			return "L" + t.toLocalDate().toEpochDay() + "_" + t.toLocalTime().toNanoOfDay() + "_" + c.id();
		}

		static Cursor parseLatest(String cursor) {
			if (cursor == null || cursor.isBlank()) {
				return new Cursor(null, null);
			}
			try {
				String[] p = cursor.substring(1).split("_");
				if (cursor.charAt(0) != 'L' || p.length != 3) {
					throw new IllegalArgumentException();
				}
				LocalDateTime at = LocalDate.ofEpochDay(Long.parseLong(p[0])).atStartOfDay().plusNanos(Long.parseLong(p[1]));
				return new Cursor(at, Long.parseLong(p[2]));
			} catch (RuntimeException e) {
				throw new CustomException(ErrorCode.BAD_REQUEST, "cursor 형식이 잘못됐습니다.");
			}
		}

		static String offset(int offset) {
			return "P" + offset;
		}

		static int parseOffset(String cursor) {
			if (cursor == null || cursor.isBlank()) {
				return 0;
			}
			try {
				if (cursor.charAt(0) != 'P') {
					throw new IllegalArgumentException();
				}
				return Math.max(0, Integer.parseInt(cursor.substring(1)));
			} catch (RuntimeException e) {
				throw new CustomException(ErrorCode.BAD_REQUEST, "cursor 형식이 잘못됐습니다.");
			}
		}
	}
}
