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
import com.debateseason_backend_v1.domain.article.entity.ToronchulArticleKeyword;
import com.debateseason_backend_v1.domain.article.entity.ToronchulArticleRevision;
import com.debateseason_backend_v1.domain.article.entity.ToronchulArticleSource;
import com.debateseason_backend_v1.domain.article.model.request.ArticleEditRequest;
import com.debateseason_backend_v1.domain.article.model.request.ArticleLinksRequest;
import com.debateseason_backend_v1.domain.article.model.request.ArticleStatusChangeRequest;
import com.debateseason_backend_v1.domain.article.model.request.ArticleUpsertRequest;
import com.debateseason_backend_v1.domain.article.model.response.ArticleAdminResponse;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleEventRepository;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleIssueRepository;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleKeywordRepository;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleRepository;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleRevisionRepository;
import com.debateseason_backend_v1.domain.article.repository.ToronchulArticleSourceRepository;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.media.infrastructure.repository.MediaJpaRepository;
import com.debateseason_backend_v1.domain.ontology.entity.AdminChangeLog;
import com.debateseason_backend_v1.domain.ontology.entity.AdminChangeLog.Action;
import com.debateseason_backend_v1.domain.ontology.repository.AdminChangeLogRepository;
import com.debateseason_backend_v1.domain.partner.service.PartnerNewsCache;
import com.fasterxml.jackson.databind.JsonNode;
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
	private final ToronchulArticleKeywordRepository keywordRepository;
	private final MediaJpaRepository mediaJpaRepository;
	private final IssueJpaRepository issueJpaRepository;
	private final NewsEventRepository newsEventRepository;
	private final ChangeLogWriter changeLog;
	private final ToronchulArticleRevisionRepository revisionRepository;
	private final AdminChangeLogRepository changeLogRepository;
	private final PartnerNewsCache partnerNewsCache;

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
		replaceKeywords(article.getId(), request.keywords());
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
		// 회수(HIDDEN)가 고객사 응답에 바로 반영되게 (돌발 기사는 상황이 끝나면 바로 내린다)
		partnerNewsCache.evict(articleId);
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

	/**
	 * 아티클 수정 (게시된 기사 포함). 보낸 필드만 바꾼다 — 데스크 사실 지적을 반영한 수정본, 장소 키워드 규칙 변경 등.
	 * 바뀌기 전 내용은 toronchul_article_revision 에 통째로 남기고, 이력(ARTICLE_EDIT)으로 되돌릴 수 있다.
	 * 고객사 응답 캐시를 바로 비운다. 공개(웹) 응답은 캐시가 없어 즉시 반영된다.
	 */
	@Transactional
	public ArticleAdminResponse.Edit edit(Long articleId, ArticleEditRequest request, String decidedBy) {

		Optional<AdminChangeLog> done = changeLogRepository.findByProposalIdAndAction(request.proposalId(), Action.ARTICLE_EDIT);
		if (done.isPresent()) {
			ToronchulArticle article = find(done.get().getEntityId());
			JsonNode before = changeLog.read(done.get().getBeforeValue());
			return new ArticleAdminResponse.Edit(article.getId(), false, article.getUpdatedAt(),
				before.path("revisionId").asLong(), done.get().getId());
		}

		ToronchulArticle article = find(articleId);
		Map<String, Object> current = editable(article);
		Map<String, Object> next = new LinkedHashMap<>(current);
		if (request.headline() != null) {
			next.put("headline", request.headline());
		}
		if (request.summary() != null) {
			next.put("summary", request.summary());
		}
		if (request.body() != null) {
			next.put("body", changeLog.write(request.body()));
		}
		if (request.keywords() != null) {
			next.put("keywords", changeLog.write(normalizeKeywords(request.keywords())));
		}
		if (request.articleSection() != null) {
			next.put("articleSection", request.articleSection());
		}
		if (next.equals(current)) {
			return new ArticleAdminResponse.Edit(articleId, false, article.getUpdatedAt(), null, null);
		}

		ToronchulArticleRevision revision = revisionRepository.save(ToronchulArticleRevision.builder()
			.articleId(articleId)
			.headline(article.getHeadline())
			.summary(article.getSummary())
			.body(article.getBody())
			.keywords(article.getKeywords())
			.articleSection(article.getArticleSection())
			.createdAt(LocalDateTime.now())
			.build());
		apply(article, next);
		article = articleRepository.saveAndFlush(article);

		AdminChangeLog log = changeLog.record(Action.ARTICLE_EDIT, ARTICLE, articleId, Map.of("revisionId", revision.getId()),
			next, request.reason(), request.proposalId(), decidedBy, request.approver());
		partnerNewsCache.evict(articleId);
		return new ArticleAdminResponse.Edit(articleId, true, article.getUpdatedAt(), revision.getId(), log.getId());
	}

	/**
	 * 수정 되돌리기 (POST /admin/change-log/{id}/revert 에서 부른다). 그 뒤 또 고쳐졌으면 덮어쓰지 않는다 — 409 REVERT_CONFLICT.
	 */
	@Transactional
	public void revertEdit(Long articleId, JsonNode before, JsonNode after) {
		ToronchulArticle article = find(articleId);
		Map<String, Object> afterFields = new LinkedHashMap<>();
		after.fieldNames().forEachRemaining(f -> afterFields.put(f, after.path(f).isNull() ? null : after.path(f).asText()));
		if (!editable(article).equals(afterFields)) {
			throw new CustomException(ErrorCode.REVERT_CONFLICT);
		}
		ToronchulArticleRevision revision = revisionRepository.findById(before.path("revisionId").asLong())
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHANGE_LOG));
		Map<String, Object> restored = new LinkedHashMap<>();
		restored.put("headline", revision.getHeadline());
		restored.put("summary", revision.getSummary());
		restored.put("body", revision.getBody());
		restored.put("keywords", revision.getKeywords());
		restored.put("articleSection", revision.getArticleSection());
		apply(article, restored);
		articleRepository.saveAndFlush(article);
		partnerNewsCache.evict(articleId);
	}

	private List<String> currentKeywords(ToronchulArticle article) {
		List<String> list = new java.util.ArrayList<>();
		if (article.getKeywords() != null) {
			changeLog.read(article.getKeywords()).forEach(k -> list.add(k.asText()));
		}
		return list;
	}

	private Map<String, Object> editable(ToronchulArticle a) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("headline", a.getHeadline());
		m.put("summary", a.getSummary());
		m.put("body", a.getBody());
		m.put("keywords", a.getKeywords());
		m.put("articleSection", a.getArticleSection());
		return m;
	}

	private void apply(ToronchulArticle a, Map<String, Object> fields) {
		a.setHeadline((String)fields.get("headline"));
		a.setSummary((String)fields.get("summary"));
		a.setBody((String)fields.get("body"));
		a.setKeywords((String)fields.get("keywords"));
		a.setArticleSection((String)fields.get("articleSection"));
		List<String> keywords = new java.util.ArrayList<>();
		if (a.getKeywords() != null) {
			changeLog.read(a.getKeywords()).forEach(k -> keywords.add(k.asText()));
		}
		replaceKeywords(a.getId(), keywords);
	}

	private static List<String> normalizeKeywords(List<String> keywords) {
		return keywords.stream().filter(Objects::nonNull).map(String::trim).filter(k -> !k.isEmpty()).distinct().toList();
	}

	@Transactional(readOnly = true)
	public ArticleAdminResponse.Summary findByKey(String articleKey) {
		ToronchulArticle article = articleRepository.findByArticleKey(articleKey)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ARTICLE));
		Map<String, List<Long>> links = linksOf(article.getId());
		return new ArticleAdminResponse.Summary(article.getId(), article.getArticleKey(), article.getEdition(),
			article.getStatus(), article.getHeadline(), article.getDatePublished(), article.getUpdatedAt(),
			links.get("events"), links.get("issues"), links.get("sources").size(), currentKeywords(article));
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

	/**
	 * 고객사 키워드 조회용 색인. 정확 일치로 찾으므로 앞뒤 공백만 자르고 표기는 그대로 둔다
	 * (데이터 쪽이 티머니 키워드 목록 표기로 정규화해 보낸다). 부분 일치를 하지 않는 이유: "강남" 이 "강남구" 를 잡으면 안 된다.
	 */
	private void replaceKeywords(Long articleId, List<String> keywords) {
		keywordRepository.deleteByArticleId(articleId);
		if (keywords == null) {
			return;
		}
		keywords.stream()
			.filter(Objects::nonNull)
			.map(String::trim)
			.filter(k -> !k.isEmpty())
			.distinct()
			.forEach(k -> keywordRepository.save(ToronchulArticleKeyword.builder().articleId(articleId).keyword(k).build()));
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
