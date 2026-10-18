package com.debateseason_backend_v1.domain.ontology.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.media.infrastructure.repository.MediaJpaRepository;
import com.debateseason_backend_v1.domain.ontology.entity.AdminChangeLog;
import com.debateseason_backend_v1.domain.ontology.entity.AdminChangeLog.Action;
import com.debateseason_backend_v1.domain.ontology.entity.IssueNewsEvent;
import com.debateseason_backend_v1.domain.ontology.entity.IssueTimelineVersion;
import com.debateseason_backend_v1.domain.ontology.entity.NewsEvent;
import com.debateseason_backend_v1.domain.ontology.entity.NewsEventMedia;
import com.debateseason_backend_v1.domain.ontology.model.request.EventMergeRequest;
import com.debateseason_backend_v1.domain.ontology.model.request.EventUpdateRequest;
import com.debateseason_backend_v1.domain.ontology.model.request.RevertRequest;
import com.debateseason_backend_v1.domain.ontology.model.response.ChangeLogResponse;
import com.debateseason_backend_v1.domain.ontology.model.response.RevertResponse;
import com.debateseason_backend_v1.domain.ontology.model.response.TimelineVersionResponse;
import com.debateseason_backend_v1.domain.ontology.repository.AdminChangeLogRepository;
import com.debateseason_backend_v1.domain.ontology.repository.IssueNewsEventRepository;
import com.debateseason_backend_v1.domain.ontology.repository.IssueTimelineVersionRepository;
import com.debateseason_backend_v1.domain.ontology.repository.NewsEventMediaRepository;
import com.debateseason_backend_v1.domain.ontology.repository.NewsEventRepository;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사건 수정·병합, 그 되돌리기, 이슈 타임라인 버전.
 *
 * <p>타임라인이 바뀌는 모든 변경(사건 추가·해제·수정·병합·되돌리기)은 같은 트랜잭션 안에서
 * {@link #recordVersion} 으로 버전을 하나 올린다. 변경과 버전이 따로 놀지 않게 하려는 것이다.
 *
 * <p>되돌리기는 이력의 after 가 지금 상태와 같을 때만 한다. 그 사이 또 바뀌었으면 409 —
 * 남의 변경을 덮어쓰지 않는다. 최근 것부터 되돌리면 된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventTimelineService {

	private static final String ISSUE = "ISSUE";
	private static final String NEWS_EVENT = "NEWS_EVENT";
	private static final String CHANGE_LOG = "CHANGE_LOG";
	private static final Set<Action> REVERTIBLE = Set.of(Action.EVENT_UPDATE, Action.EVENT_MERGE);

	private final IssueJpaRepository issueJpaRepository;
	private final MediaJpaRepository mediaJpaRepository;
	private final NewsEventRepository newsEventRepository;
	private final IssueNewsEventRepository issueNewsEventRepository;
	private final NewsEventMediaRepository newsEventMediaRepository;
	private final IssueTimelineVersionRepository versionRepository;
	private final AdminChangeLogRepository changeLogRepository;
	private final ChangeLogWriter changeLog;

	// ── 사건 수정 ────────────────────────────────────────────────

	@Transactional
	public ChangeLogResponse updateEvent(Long eventId, EventUpdateRequest request, String decidedBy) {

		Optional<AdminChangeLog> done = changeLogRepository.findByProposalIdAndAction(request.proposalId(), Action.EVENT_UPDATE);
		if (done.isPresent()) {
			return new ChangeLogResponse(done.get().getId(), false);
		}

		NewsEvent event = findEvent(eventId);
		Map<String, Object> before = fields(event);

		if (request.name() != null) {
			event.setName(request.name());
		}
		if (request.major() != null) {
			event.setMajorCategory(request.major());
		}
		if (request.middle() != null) {
			event.setMiddleCategory(request.middle());
		}
		event.setWhen(request.date(), request.occurredAt());
		Map<String, Object> after = fields(event);

		List<Long> addedMediaIds = addMedia(eventId, request.addMediaIds());
		if (before.equals(after) && addedMediaIds.isEmpty()) {
			return new ChangeLogResponse(null, false);
		}

		Map<String, Object> afterWithMedia = new LinkedHashMap<>(after);
		afterWithMedia.put("addedMediaIds", addedMediaIds);
		AdminChangeLog log = changeLog.record(Action.EVENT_UPDATE, NEWS_EVENT, eventId, before, afterWithMedia,
			request.reason(), request.proposalId(), decidedBy, request.approver());

		recordVersionForIssuesOf(eventId, log.getId());
		return new ChangeLogResponse(log.getId(), true);
	}

	// ── 사건 병합 ────────────────────────────────────────────────

	@Transactional
	public ChangeLogResponse mergeEvents(Long issueId, EventMergeRequest request, String decidedBy) {

		Optional<AdminChangeLog> done = changeLogRepository.findByProposalIdAndAction(request.proposalId(), Action.EVENT_MERGE);
		if (done.isPresent()) {
			return new ChangeLogResponse(done.get().getId(), false);
		}

		requireIssue(issueId);
		Long keepId = request.keepEventId();
		Long dropId = request.dropEventId();
		NewsEvent keep = findEvent(keepId);
		NewsEvent drop = findEvent(dropId);
		Optional<IssueNewsEvent> keepLink = issueNewsEventRepository.findByIssueIdAndEventId(issueId, keepId);
		Optional<IssueNewsEvent> dropLink = issueNewsEventRepository.findByIssueIdAndEventId(issueId, dropId);
		if (keepId.equals(dropId) || keepLink.isEmpty() || dropLink.isEmpty()
			|| keep.getMergedInto() != null || drop.getMergedInto() != null) {
			throw new CustomException(ErrorCode.INVALID_EVENT_MERGE);
		}

		// drop 의 근거 기사를 keep 에 복사한다. drop 쪽 행은 남긴다 — 되돌릴 때 그대로 살아 있어야 한다.
		List<Long> dropMedia = newsEventMediaRepository.findByEventId(dropId).stream().map(NewsEventMedia::getMediaId).toList();
		List<Long> copiedMediaIds = addMedia(keepId, dropMedia);

		drop.setMergedInto(keepId);
		IssueNewsEvent unlinked = dropLink.get();
		issueNewsEventRepository.delete(unlinked);

		Map<String, Object> after = new LinkedHashMap<>();
		after.put("keepEventId", keepId);
		after.put("dropEventId", dropId);
		after.put("copiedMediaIds", copiedMediaIds);
		after.put("dropLinkApprovedBy", unlinked.getApprovedBy());
		after.put("dropLinkCreatedAt", unlinked.getCreatedAt().toString());
		AdminChangeLog log = changeLog.record(Action.EVENT_MERGE, ISSUE, issueId,
			Map.of("eventIds", List.of(keepId, dropId)), after,
			request.reason(), request.proposalId(), decidedBy, request.approver());

		recordVersion(issueId, log.getId());
		return new ChangeLogResponse(log.getId(), true);
	}

	// ── 되돌리기 ────────────────────────────────────────────────

	@Transactional
	public RevertResponse revert(Long changeLogId, RevertRequest request, String decidedBy) {

		String revertKey = "revert:" + changeLogId;
		Optional<AdminChangeLog> done = changeLogRepository.findByProposalIdAndAction(revertKey, Action.REVERT);
		if (done.isPresent()) {
			return new RevertResponse(done.get().getId(), changeLogId, false);
		}

		AdminChangeLog target = changeLogRepository.findById(changeLogId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHANGE_LOG));
		if (!REVERTIBLE.contains(target.getAction())) {
			throw new CustomException(ErrorCode.REVERT_NOT_SUPPORTED);
		}

		JsonNode before = changeLog.read(target.getBeforeValue());
		JsonNode after = changeLog.read(target.getAfterValue());
		List<Long> affectedIssues = target.getAction() == Action.EVENT_UPDATE
			? revertEventUpdate(target.getEntityId(), before, after)
			: revertEventMerge(target.getEntityId(), after);

		AdminChangeLog log = changeLog.record(Action.REVERT, CHANGE_LOG, changeLogId,
			Map.of("action", target.getAction().name()), null,
			request.reason(), revertKey, decidedBy, request.approver());
		affectedIssues.forEach(issueId -> recordVersion(issueId, log.getId()));

		return new RevertResponse(log.getId(), changeLogId, true);
	}

	private List<Long> revertEventUpdate(Long eventId, JsonNode before, JsonNode after) {
		NewsEvent event = findEvent(eventId);
		if (!fields(event).equals(fieldsOf(after))) {
			throw new CustomException(ErrorCode.REVERT_CONFLICT);
		}

		event.setName(text(before, "name"));
		event.setMajorCategory(text(before, "major"));
		event.setMiddleCategory(text(before, "middle"));
		String date = text(before, "date");
		String at = text(before, "occurredAt");
		event.setOccurredOn(date == null ? null : LocalDate.parse(date));
		event.setOccurredAt(at == null ? null : LocalDateTime.parse(at));
		removeMedia(eventId, longs(after.path("addedMediaIds")));

		return issueNewsEventRepository.findByEventId(eventId).stream().map(IssueNewsEvent::getIssueId).toList();
	}

	private List<Long> revertEventMerge(Long issueId, JsonNode after) {
		Long keepId = after.path("keepEventId").asLong();
		Long dropId = after.path("dropEventId").asLong();
		NewsEvent drop = findEvent(dropId);
		if (!Objects.equals(drop.getMergedInto(), keepId)
			|| issueNewsEventRepository.findByIssueIdAndEventId(issueId, dropId).isPresent()) {
			throw new CustomException(ErrorCode.REVERT_CONFLICT);
		}

		drop.setMergedInto(null);
		issueNewsEventRepository.save(IssueNewsEvent.builder()
			.issueId(issueId)
			.eventId(dropId)
			.approvedBy(text(after, "dropLinkApprovedBy"))
			.createdAt(LocalDateTime.parse(after.path("dropLinkCreatedAt").asText()))
			.build());
		removeMedia(keepId, longs(after.path("copiedMediaIds")));

		return List.of(issueId);
	}

	// ── 타임라인 버전 ────────────────────────────────────────────

	/** 이슈에 지금 연결된 사건으로 스냅샷을 찍어 버전을 하나 올린다. 호출 측 트랜잭션 안에서 부른다. */
	public void recordVersion(Long issueId, Long changeLogId) {
		int next = versionRepository.findFirstByIssueIdOrderByVersionDesc(issueId)
			.map(v -> v.getVersion() + 1)
			.orElse(1);

		versionRepository.save(IssueTimelineVersion.builder()
			.issueId(issueId)
			.version(next)
			.changeLogId(changeLogId)
			.snapshot(changeLog.write(snapshot(issueId)))
			.createdAt(LocalDateTime.now())
			.build());
	}

	@Transactional(readOnly = true)
	public List<TimelineVersionResponse> versions(Long issueId) {
		requireIssue(issueId);
		return versionRepository.findByIssueIdOrderByVersionDesc(issueId).stream()
			.map(v -> new TimelineVersionResponse(v.getVersion(), v.getChangeLogId(), changeLog.read(v.getSnapshot()), v.getCreatedAt()))
			.toList();
	}

	private void recordVersionForIssuesOf(Long eventId, Long changeLogId) {
		issueNewsEventRepository.findByEventId(eventId)
			.forEach(link -> recordVersion(link.getIssueId(), changeLogId));
	}

	private List<Map<String, Object>> snapshot(Long issueId) {
		List<Long> eventIds = issueNewsEventRepository.findByIssueId(issueId).stream().map(IssueNewsEvent::getEventId).toList();
		List<NewsEvent> events = new ArrayList<>(newsEventRepository.findAllById(eventIds));
		events.sort(Comparator.comparing(NewsEvent::when, Comparator.nullsLast(Comparator.<LocalDateTime>naturalOrder()))
			.thenComparing(NewsEvent::getId));

		List<Map<String, Object>> rows = new ArrayList<>();
		for (NewsEvent event : events) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("id", event.getId());
			row.put("name", event.getName());
			row.put("date", event.getOccurredOn() == null ? null : event.getOccurredOn().toString());
			row.put("occurredAt", event.getOccurredAt() == null ? null : event.getOccurredAt().toString());
			rows.add(row);
		}
		return rows;
	}

	// ── 공용 ────────────────────────────────────────────────────

	/** 근거 기사를 붙인다. 이미 붙은 것은 건너뛰고, 실제로 붙인 media id 만 돌려준다. 하나라도 없는 id 면 404. */
	List<Long> addMedia(Long eventId, Collection<Long> mediaIds) {
		if (mediaIds == null || mediaIds.isEmpty()) {
			return List.of();
		}
		Set<Long> requested = new LinkedHashSet<>(mediaIds);
		requireMedia(requested);

		Set<Long> existing = new LinkedHashSet<>();
		newsEventMediaRepository.findByEventIdAndMediaIdIn(eventId, requested).forEach(m -> existing.add(m.getMediaId()));

		List<Long> added = new ArrayList<>();
		for (Long mediaId : requested) {
			if (!existing.contains(mediaId)) {
				newsEventMediaRepository.save(NewsEventMedia.builder().eventId(eventId).mediaId(mediaId).build());
				added.add(mediaId);
			}
		}
		return added;
	}

	private void removeMedia(Long eventId, List<Long> mediaIds) {
		if (!mediaIds.isEmpty()) {
			newsEventMediaRepository.deleteAll(newsEventMediaRepository.findByEventIdAndMediaIdIn(eventId, mediaIds));
		}
	}

	void requireMedia(Set<Long> mediaIds) {
		if (mediaIds.isEmpty()) {
			return;
		}
		Set<Long> found = new LinkedHashSet<>();
		mediaJpaRepository.findAllById(mediaIds).forEach(media -> found.add(media.getId()));
		if (found.size() != mediaIds.size()) {
			Set<Long> missing = new LinkedHashSet<>(mediaIds);
			missing.removeAll(found);
			throw new CustomException(ErrorCode.MEDIA_NOT_FOUND, "존재하지 않는 media id: " + missing);
		}
	}

	private void requireIssue(Long issueId) {
		if (!issueJpaRepository.existsById(issueId)) {
			throw new CustomException(ErrorCode.NOT_FOUND_ISSUE);
		}
	}

	private NewsEvent findEvent(Long eventId) {
		return newsEventRepository.findById(eventId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_EVENT));
	}

	private static Map<String, Object> fields(NewsEvent event) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", event.getName());
		map.put("major", event.getMajorCategory());
		map.put("middle", event.getMiddleCategory());
		map.put("date", event.getOccurredOn() == null ? null : event.getOccurredOn().toString());
		map.put("occurredAt", event.getOccurredAt() == null ? null : event.getOccurredAt().toString());
		return map;
	}

	private static Map<String, Object> fieldsOf(JsonNode node) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", text(node, "name"));
		map.put("major", text(node, "major"));
		map.put("middle", text(node, "middle"));
		map.put("date", text(node, "date"));
		map.put("occurredAt", text(node, "occurredAt"));
		return map;
	}

	private static String text(JsonNode node, String field) {
		JsonNode value = node.path(field);
		return value.isMissingNode() || value.isNull() ? null : value.asText();
	}

	private static List<Long> longs(JsonNode array) {
		List<Long> values = new ArrayList<>();
		array.forEach(v -> values.add(v.asLong()));
		return values;
	}
}
