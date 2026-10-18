package com.debateseason_backend_v1.domain.article.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.article.entity.ArticleStatus;
import com.debateseason_backend_v1.domain.article.entity.ToronchulArticle;
import com.debateseason_backend_v1.domain.article.entity.ToronchulArticleEvent;
import com.debateseason_backend_v1.domain.article.entity.ToronchulArticleIssue;
import com.debateseason_backend_v1.domain.article.model.response.ArticlePublicResponse;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleEventRepository;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleIssueRepository;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleRepository;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;
import com.debateseason_backend_v1.domain.ontology.entity.IssueNewsEvent;
import com.debateseason_backend_v1.domain.ontology.entity.NewsEvent;
import com.debateseason_backend_v1.domain.ontology.repository.IssueNewsEventRepository;
import com.debateseason_backend_v1.domain.ontology.repository.NewsEventRepository;
import com.debateseason_backend_v1.domain.ontology.service.ChangeLogWriter;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

/**
 * 토론철 아티클·사건 타임라인 공개 조회 (웹 v1.1.0, 익명).
 *
 * <p>노출 규칙: 아티클은 PUBLISHED + toronchul 판만. 이슈는 ARCHIVED 를 뺀다(DORMANT 는 노출).
 * 병합된 사건(merged_into)은 따로 내지 않고 흡수한 사건으로 보인다 — 아티클 연결이 끊기지 않게.
 * 고객사(티머니) B2B 조회와 경로를 섞지 않는다(크롤러 트래픽이 미터링에 섞인다).
 */
@Service
@RequiredArgsConstructor
public class ArticlePublicService {

	static final int DEFAULT_SIZE = 20;
	static final int MAX_SIZE = 100;

	private final ToronchulArticleRepository articleRepository;
	private final ToronchulArticleEventRepository eventLinkRepository;
	private final ToronchulArticleIssueRepository issueLinkRepository;
	private final NewsEventRepository newsEventRepository;
	private final IssueNewsEventRepository issueNewsEventRepository;
	private final IssueJpaRepository issueJpaRepository;
	private final ChangeLogWriter json;

	@Transactional(readOnly = true)
	public ArticlePublicResponse.Detail article(Long articleId) {
		ToronchulArticle article = articleRepository.findById(articleId)
			.filter(ArticlePublicService::isPublic)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ARTICLE));

		// 사건: 병합된 사건은 흡수한 사건으로 바꾸고 중복을 없앤다
		List<Long> linked = eventLinkRepository.findByArticleId(articleId).stream().map(ToronchulArticleEvent::getNewsEventId).toList();
		List<NewsEvent> events = resolveMerged(linked);
		Map<Long, List<Long>> issuesByEvent = visibleIssuesByEvent(events.stream().map(NewsEvent::getId).toList());

		Set<Long> issueIds = new LinkedHashSet<>();
		issuesByEvent.values().forEach(issueIds::addAll);
		List<Long> direct = issueLinkRepository.findByArticleId(articleId).stream().map(ToronchulArticleIssue::getIssueId).toList();
		issueIds.addAll(visible(direct));

		return new ArticlePublicResponse.Detail(
			article.getId(), article.getHeadline(), article.getSummary(), paragraphs(article.getBody()),
			article.getByline(), article.isAiGenerated(), article.getArticleSection(), strings(article.getKeywords()),
			article.getDatePublished(), article.getUpdatedAt(),
			events.stream().map(e -> new ArticlePublicResponse.Event(e.getId(), e.getName(), e.getOccurredOn(), e.getOccurredAt(),
				issuesByEvent.getOrDefault(e.getId(), List.of()))).toList(),
			issueIds.stream().sorted().toList());
	}

	/**
	 * 이슈의 사건 타임라인. 사건 수가 이슈당 많지 않아(수십 건) 메모리에서 정렬·자른다.
	 * cursor = "{발생일 또는 -}:{사건 id}" — 정렬 키 그대로라 중간에 사건이 늘어도 건너뛰지 않는다.
	 */
	@Transactional(readOnly = true)
	public ArticlePublicResponse.Timeline timeline(Long issueId, String cursor, Integer size) {
		IssueEntity issue = issueJpaRepository.findById(issueId)
			.filter(i -> i.getStatus() != IssueStatus.ARCHIVED)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ISSUE));

		List<Long> eventIds = issueNewsEventRepository.findByIssueId(issue.getId()).stream().map(IssueNewsEvent::getEventId).toList();
		List<NewsEvent> events = new ArrayList<>(newsEventRepository.findAllById(eventIds).stream()
			.filter(e -> e.getMergedInto() == null).toList());
		Comparator<NewsEvent> order = Comparator
			.comparing(NewsEvent::when, Comparator.nullsFirst(Comparator.<LocalDateTime>naturalOrder())).reversed()
			.thenComparing(NewsEvent::getId, Comparator.reverseOrder());
		events.sort(order);

		int start = 0;
		if (cursor != null && !cursor.isBlank()) {
			NewsEvent marker = parseCursor(cursor);
			while (start < events.size() && order.compare(events.get(start), marker) <= 0) {
				start++;
			}
		}
		int limit = clamp(size);
		List<NewsEvent> page = events.subList(start, Math.min(events.size(), start + limit));
		String next = start + limit < events.size() ? cursorOf(page.get(page.size() - 1)) : null;

		Map<Long, List<ArticlePublicResponse.ArticleCard>> cards = articlesByEvent(page.stream().map(NewsEvent::getId).toList());
		return new ArticlePublicResponse.Timeline(
			page.stream().map(e -> new ArticlePublicResponse.TimelineEvent(e.getId(), e.getName(), e.getOccurredOn(), e.getOccurredAt(),
				cards.getOrDefault(e.getId(), List.of()))).toList(),
			next);
	}

	// ── 내부 ────────────────────────────────────────────────────

	static boolean isPublic(ToronchulArticle a) {
		return a.getStatus() == ArticleStatus.PUBLISHED && ToronchulArticle.TORONCHUL_EDITION.equals(a.getEdition());
	}

	private Map<Long, List<ArticlePublicResponse.ArticleCard>> articlesByEvent(List<Long> eventIds) {
		if (eventIds.isEmpty()) {
			return Map.of();
		}
		// 흡수된 사건에 붙은 아티클도 흡수한 사건 밑에 보인다
		List<NewsEvent> absorbed = newsEventRepository.findAll().stream()
			.filter(e -> e.getMergedInto() != null && eventIds.contains(e.getMergedInto())).toList();
		Map<Long, Long> owner = new LinkedHashMap<>();
		eventIds.forEach(id -> owner.put(id, id));
		absorbed.forEach(e -> owner.put(e.getId(), e.getMergedInto()));

		List<ToronchulArticleEvent> links = eventLinkRepository.findByNewsEventIdIn(owner.keySet());
		Map<Long, ToronchulArticle> articles = articleRepository.findAllById(
				links.stream().map(ToronchulArticleEvent::getArticleId).distinct().toList()).stream()
			.filter(ArticlePublicService::isPublic)
			.collect(Collectors.toMap(ToronchulArticle::getId, Function.identity()));

		Map<Long, List<ArticlePublicResponse.ArticleCard>> result = new LinkedHashMap<>();
		for (ToronchulArticleEvent link : links) {
			ToronchulArticle a = articles.get(link.getArticleId());
			if (a == null) {
				continue;
			}
			List<ArticlePublicResponse.ArticleCard> list = result.computeIfAbsent(owner.get(link.getNewsEventId()), k -> new ArrayList<>());
			if (list.stream().noneMatch(c -> c.articleId().equals(a.getId()))) {
				list.add(new ArticlePublicResponse.ArticleCard(a.getId(), a.getHeadline(), a.getSummary(), a.getDatePublished()));
			}
		}
		result.values().forEach(list -> list.sort(Comparator.comparing(ArticlePublicResponse.ArticleCard::datePublished,
			Comparator.nullsLast(Comparator.<java.time.LocalDateTime>reverseOrder()))));
		return result;
	}

	private List<NewsEvent> resolveMerged(List<Long> eventIds) {
		Map<Long, NewsEvent> byId = new LinkedHashMap<>();
		for (NewsEvent e : newsEventRepository.findAllById(eventIds)) {
			NewsEvent target = e;
			// 병합 사슬을 따라간다 (A→B→C). 순환은 병합 API 가 막지만 방어로 10단계에서 끊는다.
			for (int hop = 0; hop < 10 && target.getMergedInto() != null; hop++) {
				target = newsEventRepository.findById(target.getMergedInto()).orElse(target);
			}
			byId.putIfAbsent(target.getId(), target);
		}
		return new ArrayList<>(byId.values());
	}

	private Map<Long, List<Long>> visibleIssuesByEvent(Collection<Long> eventIds) {
		Map<Long, List<Long>> result = new LinkedHashMap<>();
		for (Long eventId : eventIds) {
			List<Long> issues = issueNewsEventRepository.findByEventId(eventId).stream().map(IssueNewsEvent::getIssueId).toList();
			result.put(eventId, visible(issues).stream().sorted().toList());
		}
		return result;
	}

	private List<Long> visible(List<Long> issueIds) {
		if (issueIds.isEmpty()) {
			return List.of();
		}
		return issueJpaRepository.findAllById(issueIds).stream()
			.filter(i -> i.getStatus() != IssueStatus.ARCHIVED)
			.map(IssueEntity::getId)
			.toList();
	}

	private List<String> paragraphs(String body) {
		List<String> texts = new ArrayList<>();
		for (JsonNode p : json.read(body)) {
			String text = p.path("text").asText(null);
			if (text != null && !text.isBlank()) {
				texts.add(text);
			}
		}
		return texts;
	}

	private List<String> strings(String array) {
		List<String> values = new ArrayList<>();
		if (array != null) {
			json.read(array).forEach(v -> values.add(v.asText()));
		}
		return values;
	}

	// "{정렬 시각 또는 -}|{사건 id}". 옛 형식 "{날짜 또는 -}:{id}" 도 읽는다 (이미 나간 커서).
	private static String cursorOf(NewsEvent e) {
		return (e.when() == null ? "-" : e.when().toString()) + "|" + e.getId();
	}

	private static NewsEvent parseCursor(String cursor) {
		try {
			boolean current = cursor.contains("|");
			String[] parts = cursor.split(current ? "\\|" : ":", 2);
			NewsEvent marker = NewsEvent.builder().id(Long.parseLong(parts[1])).build();
			if (!"-".equals(parts[0])) {
				if (current) {
					marker.setOccurredAt(LocalDateTime.parse(parts[0]));
				} else {
					marker.setOccurredOn(LocalDate.parse(parts[0]));
				}
			}
			return marker;
		} catch (RuntimeException e) {
			throw new CustomException(ErrorCode.BAD_REQUEST, "cursor 형식이 잘못됐습니다.");
		}
	}

	private static int clamp(Integer size) {
		if (size == null || size <= 0) {
			return DEFAULT_SIZE;
		}
		return Math.min(size, MAX_SIZE);
	}
}
