package com.debateseason_backend_v1.domain.article.service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.article.entity.ArticleStatus;
import com.debateseason_backend_v1.domain.article.entity.ToronchulArticle;
import com.debateseason_backend_v1.domain.article.entity.ToronchulArticleEvent;
import com.debateseason_backend_v1.domain.article.entity.ToronchulArticleIssue;
import com.debateseason_backend_v1.domain.article.entity.ToronchulArticleSource;
import com.debateseason_backend_v1.domain.article.model.request.ArticleLinksRequest;
import com.debateseason_backend_v1.domain.article.model.request.ArticleStatusChangeRequest;
import com.debateseason_backend_v1.domain.article.model.request.ArticleUpsertRequest;
import com.debateseason_backend_v1.domain.article.model.response.ArticleAdminResponse;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleEventRepository;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleIssueRepository;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleRepository;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleSourceRepository;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.media.infrastructure.repository.MediaJpaRepository;
import com.debateseason_backend_v1.domain.ontology.entity.AdminChangeLog.Action;
import com.debateseason_backend_v1.domain.ontology.repository.NewsEventRepository;
import com.debateseason_backend_v1.domain.ontology.service.ChangeLogWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 토론철 아티클 적재·상태·연결 (데이터 레포가 서비스 키로 부른다).
 *
 * <p>흐름 (사용자 결정 2026-10-19): 데이터 쪽이 데스킹을 마친 기사만 보내고, 적재 직후 PUBLISHED 로 바꾼다.
 * Discord 승인은 없다. 회수는 PUBLISHED → HIDDEN.
 *
 * <p>한 번이라도 게시된(date_published 가 있는) 기사는 같은 articleKey 로 다시 적재할 수 없다(409).
 * 공개된 본문이 조용히 바뀌지 않게 하려는 것이다. 본문 수정은 리비전 테이블과 함께 따로 연다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleAdminService {

	private static final String ARTICLE = "ARTICLE";

	// 열린 전이. APPROVED·REJECTED·EXCLUDED 는 저장만 받는다 (데이터 PRD 의 상태 집합 호환).
	private static final Map<ArticleStatus, Set<ArticleStatus>> TRANSITIONS = Map.of(
		ArticleStatus.REVIEW, EnumSet.of(ArticleStatus.PUBLISHED, ArticleStatus.HIDDEN),
		ArticleStatus.PUBLISHED, EnumSet.of(ArticleStatus.HIDDEN),
		ArticleStatus.HIDDEN, EnumSet.of(ArticleStatus.PUBLISHED)
	);

	private final ToronchulArticleRepository articleRepository;
	private final ToronchulArticleSourceRepository sourceRepository;
	private final ToronchulArticleEventRepository eventLinkRepository;
	private final ToronchulArticleIssueRepository issueLinkRepository;
	private final MediaJpaRepository mediaJpaRepository;
	private final IssueJpaRepository issueJpaRepository;
	private final NewsEventRepository newsEventRepository;
	private final ChangeLogWriter changeLog;

	@Transactional
	public ArticleAdminResponse.Upsert upsert(ArticleUpsertRequest request, String decidedBy) {

		Optional<ToronchulArticle> existing = articleRepository.findByArticleKey(request.articleKey());
		if (existing.isPresent() && existing.get().getDatePublished() != null) {
			throw new CustomException(ErrorCode.ARTICLE_ALREADY_PUBLISHED);
		}

		List<Long> sources = distinct(request.sourceMediaIds());
		List<Long> issues = distinct(request.issueIds());
		List<Long> events = distinct(request.newsEventIds());
		requireMedia(sources);
		requireIssues(issues);
		requireEvents(events);

		String body = changeLog.write(request.body());
		String keywords = request.keywords() == null ? null : changeLog.write(request.keywords());
		String axes = request.sensitivityAxes() == null ? null : changeLog.write(request.sensitivityAxes());

		ToronchulArticle article = existing.orElseGet(() -> ToronchulArticle.builder()
			.articleKey(request.articleKey())
			.status(ArticleStatus.REVIEW)
			.viewCount(0)
			.build());
		String before = existing.isPresent() ? fingerprint(article, linksOf(article.getId())) : null;

		article.setEdition(request.edition());
		article.setHeadline(request.headline());
		article.setSummary(request.summary());
		article.setBody(body);
		article.setArticleSection(request.articleSection());
		article.setKeywords(keywords);
		article.setDebatePoint(request.debatePoint());
		article.setByline(request.byline() == null || request.byline().isBlank()
			? ToronchulArticle.DEFAULT_BYLINE : request.byline());
		article.setAiGenerated(request.aiGenerated() == null || request.aiGenerated());
		article.setSensitivityMax(request.sensitivityMax());
		article.setSensitivityAxes(axes);
		article.setGeneratorVersion(request.generatorVersion());
		if (article.getCreatedAt() == null || request.createdAt() != null) {
			article.setCreatedAt(request.createdAt() != null ? request.createdAt() : LocalDateTime.now());
		}
		article = articleRepository.save(article);

		replaceSources(article.getId(), sources);
		replaceIssues(article.getId(), issues);
		replaceEvents(article.getId(), events);

		String after = fingerprint(article, Map.of("sources", sources, "issues", issues, "events", events));
		boolean changed = !after.equals(before);
		if (changed) {
			changeLog.record(Action.ARTICLE_UPSERT, ARTICLE, article.getId(), null,
				Map.of("articleKey", article.getArticleKey(), "headline", article.getHeadline(), "created", before == null),
				null, request.articleKey(), decidedBy, request.approver());
		}
		return new ArticleAdminResponse.Upsert(article.getId(), article.getStatus(), changed);
	}

	@Transactional
	public ArticleAdminResponse.StatusChange changeStatus(Long articleId, ArticleStatusChangeRequest request, String decidedBy) {

		ToronchulArticle article = find(articleId);
		ArticleStatus current = article.getStatus();
		ArticleStatus target = request.status();
		if (current == target) {
			return new ArticleAdminResponse.StatusChange(articleId, current, current, false, article.getDatePublished());
		}
		if (!TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
			throw new CustomException(ErrorCode.INVALID_ARTICLE_STATUS_TRANSITION);
		}

		article.setStatus(target);
		article.setReviewedAt(LocalDateTime.now());
		article.setReviewedBy(request.approver() != null ? request.approver() : decidedBy);
		if (target == ArticleStatus.PUBLISHED && article.getDatePublished() == null) {
			article.setDatePublished(LocalDateTime.now());
		}

		changeLog.record(Action.ARTICLE_STATUS, ARTICLE, articleId, Map.of("status", current.name()),
			Map.of("status", target.name()), request.reason(), null, decidedBy, request.approver());
		log.info("아티클 상태 변경 articleId={} {} -> {}", articleId, current, target);
		return new ArticleAdminResponse.StatusChange(articleId, current, target, true, article.getDatePublished());
	}

	@Transactional
	public ArticleAdminResponse.Links replaceLinks(Long articleId, ArticleLinksRequest request, String decidedBy) {

		find(articleId);
		Map<String, List<Long>> before = linksOf(articleId);
		if (request.newsEventIds() != null) {
			List<Long> events = distinct(request.newsEventIds());
			requireEvents(events);
			replaceEvents(articleId, events);
		}
		if (request.issueIds() != null) {
			List<Long> issues = distinct(request.issueIds());
			requireIssues(issues);
			replaceIssues(articleId, issues);
		}
		Map<String, List<Long>> after = linksOf(articleId);
		if (!after.equals(before)) {
			changeLog.record(Action.ARTICLE_LINKS, ARTICLE, articleId, before, after, null, null, decidedBy, request.approver());
		}
		return new ArticleAdminResponse.Links(articleId, after.get("events"), after.get("issues"));
	}

	@Transactional(readOnly = true)
	public ArticleAdminResponse.Summary findByKey(String articleKey) {
		ToronchulArticle article = articleRepository.findByArticleKey(articleKey)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ARTICLE));
		Map<String, List<Long>> links = linksOf(article.getId());
		return new ArticleAdminResponse.Summary(article.getId(), article.getArticleKey(), article.getEdition(),
			article.getStatus(), article.getHeadline(), article.getDatePublished(), article.getUpdatedAt(),
			links.get("events"), links.get("issues"), links.get("sources").size());
	}

	// ── 내부 ────────────────────────────────────────────────────

	private ToronchulArticle find(Long articleId) {
		return articleRepository.findById(articleId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ARTICLE));
	}

	private Map<String, List<Long>> linksOf(Long articleId) {
		Map<String, List<Long>> links = new LinkedHashMap<>();
		links.put("sources", sourceRepository.findByArticleId(articleId).stream().map(ToronchulArticleSource::getMediaId).sorted().toList());
		links.put("issues", issueLinkRepository.findByArticleId(articleId).stream().map(ToronchulArticleIssue::getIssueId).sorted().toList());
		links.put("events", eventLinkRepository.findByArticleId(articleId).stream().map(ToronchulArticleEvent::getNewsEventId).sorted().toList());
		return links;
	}

	private String fingerprint(ToronchulArticle a, Map<String, List<Long>> links) {
		Map<String, Object> f = new LinkedHashMap<>();
		f.put("edition", a.getEdition());
		f.put("headline", a.getHeadline());
		f.put("summary", a.getSummary());
		f.put("body", a.getBody());
		f.put("section", a.getArticleSection());
		f.put("keywords", a.getKeywords());
		f.put("debatePoint", a.getDebatePoint());
		f.put("byline", a.getByline());
		f.put("ai", a.isAiGenerated());
		f.put("sens", a.getSensitivityMax() == null ? null : a.getSensitivityMax().stripTrailingZeros().toPlainString());
		f.put("axes", a.getSensitivityAxes());
		f.put("gen", a.getGeneratorVersion());
		// 키 순서를 고정한다. Map.of 는 순서가 없어 같은 내용이라도 지문이 달라졌다.
		Map<String, List<Long>> sorted = new LinkedHashMap<>();
		for (String k : List.of("sources", "issues", "events")) {
			sorted.put(k, links.getOrDefault(k, List.of()).stream().sorted().toList());
		}
		f.put("links", sorted);
		return changeLog.write(f);
	}

	private void replaceSources(Long articleId, List<Long> mediaIds) {
		sourceRepository.deleteByArticleId(articleId);
		mediaIds.forEach(id -> sourceRepository.save(ToronchulArticleSource.builder().articleId(articleId).mediaId(id).build()));
	}

	private void replaceIssues(Long articleId, List<Long> issueIds) {
		issueLinkRepository.deleteByArticleId(articleId);
		issueIds.forEach(id -> issueLinkRepository.save(ToronchulArticleIssue.builder().articleId(articleId).issueId(id).build()));
	}

	private void replaceEvents(Long articleId, List<Long> eventIds) {
		eventLinkRepository.deleteByArticleId(articleId);
		eventIds.forEach(id -> eventLinkRepository.save(ToronchulArticleEvent.builder().articleId(articleId).newsEventId(id).build()));
	}

	private void requireMedia(List<Long> ids) {
		if (ids.isEmpty()) {
			return;
		}
		Set<Long> found = new LinkedHashSet<>();
		mediaJpaRepository.findAllById(ids).forEach(m -> found.add(m.getId()));
		requireAll(ids, found, ErrorCode.MEDIA_NOT_FOUND, "존재하지 않는 media id: ");
	}

	private void requireIssues(List<Long> ids) {
		Set<Long> found = new LinkedHashSet<>();
		issueJpaRepository.findAllById(ids).forEach(i -> found.add(i.getId()));
		requireAll(ids, found, ErrorCode.NOT_FOUND_ISSUE, "존재하지 않는 issue id: ");
	}

	private void requireEvents(List<Long> ids) {
		Set<Long> found = new LinkedHashSet<>();
		newsEventRepository.findAllById(ids).forEach(e -> found.add(e.getId()));
		requireAll(ids, found, ErrorCode.NOT_FOUND_EVENT, "존재하지 않는 news_event id: ");
	}

	private static void requireAll(List<Long> requested, Set<Long> found, ErrorCode code, String message) {
		List<Long> missing = requested.stream().filter(id -> !found.contains(id)).toList();
		if (!missing.isEmpty()) {
			throw new CustomException(code, message + missing);
		}
	}

	private static List<Long> distinct(List<Long> ids) {
		return ids == null ? List.of() : ids.stream().filter(Objects::nonNull).distinct().toList();
	}
}
