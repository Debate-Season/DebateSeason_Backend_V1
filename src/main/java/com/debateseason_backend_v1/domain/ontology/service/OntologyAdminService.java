package com.debateseason_backend_v1.domain.ontology.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomStatus;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;
import com.debateseason_backend_v1.domain.media.infrastructure.repository.MediaJpaRepository;
import com.debateseason_backend_v1.domain.ontology.entity.AdminChangeLog;
import com.debateseason_backend_v1.domain.ontology.entity.AdminChangeLog.Action;
import com.debateseason_backend_v1.domain.ontology.entity.IssueNewsEvent;
import com.debateseason_backend_v1.domain.ontology.entity.NewsEvent;
import com.debateseason_backend_v1.domain.ontology.entity.NewsEventMedia;
import com.debateseason_backend_v1.domain.ontology.model.request.DebateTopicCreateRequest;
import com.debateseason_backend_v1.domain.ontology.model.request.EventLinkRequest;
import com.debateseason_backend_v1.domain.ontology.model.request.EventUnlinkRequest;
import com.debateseason_backend_v1.domain.ontology.model.request.IssueCreateRequest;
import com.debateseason_backend_v1.domain.ontology.model.request.IssueRenameRequest;
import com.debateseason_backend_v1.domain.ontology.model.request.TopicStatusChangeRequest;
import com.debateseason_backend_v1.domain.ontology.model.response.DebateTopicCreateResponse;
import com.debateseason_backend_v1.domain.ontology.model.response.EventLinkResponse;
import com.debateseason_backend_v1.domain.ontology.model.response.EventUnlinkResponse;
import com.debateseason_backend_v1.domain.ontology.model.response.IssueCreateResponse;
import com.debateseason_backend_v1.domain.ontology.model.response.IssueRenameResponse;
import com.debateseason_backend_v1.domain.ontology.model.response.TopicStatusChangeResponse;
import com.debateseason_backend_v1.domain.ontology.repository.AdminChangeLogRepository;
import com.debateseason_backend_v1.domain.ontology.repository.IssueNewsEventRepository;
import com.debateseason_backend_v1.domain.ontology.repository.NewsEventMediaRepository;
import com.debateseason_backend_v1.domain.ontology.repository.NewsEventRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 온톨로지 승인(✅)·롤백(❌)을 운영 데이터에 반영한다.
 *
 * <p>모든 호출은 멱등이다. 파이프라인은 실패하면 같은 요청을 다시 보낸다.
 * <ul>
 *   <li>생성 계열(이슈·토론 주제·사건)은 proposalId 로 중복을 막는다. 두 번째 호출은 처음 결과를 돌려준다.</li>
 *   <li>변경 계열(이름·상태)은 이미 그 값이면 아무것도 하지 않는다.</li>
 * </ul>
 *
 * <p>롤백은 지우지 않고 숨긴다 — 신설 이슈는 ARCHIVED, 신설 토론 주제는 HIDDEN.
 * 그 사이 사용자가 남긴 채팅·투표가 있을 수 있어서다. 사건 연결 해제만 연결 행을 지운다(사건 행은 남김).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OntologyAdminService {

	private static final String ISSUE = "ISSUE";
	private static final String CHAT_ROOM = "CHAT_ROOM";
	private static final Set<ChatRoomStatus> TOPIC_SWITCHABLE = EnumSet.of(ChatRoomStatus.OPEN, ChatRoomStatus.HIDDEN);

	private final IssueJpaRepository issueJpaRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final MediaJpaRepository mediaJpaRepository;
	private final AdminChangeLogRepository changeLogRepository;
	private final NewsEventRepository newsEventRepository;
	private final IssueNewsEventRepository issueNewsEventRepository;
	private final NewsEventMediaRepository newsEventMediaRepository;
	private final ObjectMapper objectMapper;

	// ── 이슈 신설 ────────────────────────────────────────────────

	@Transactional
	public IssueCreateResponse createIssue(IssueCreateRequest request, String decidedBy) {

		Optional<AdminChangeLog> done = changeLogRepository.findByProposalIdAndAction(request.proposalId(), Action.ISSUE_CREATE);
		if (done.isPresent()) {
			JsonNode after = readJson(done.get().getAfterValue());
			return new IssueCreateResponse(after.path("issueId").asLong(), after.path("containerRoomId").asLong(), false);
		}

		IssueEntity issue = issueJpaRepository.save(IssueEntity.builder()
			.title(request.title())
			.majorCategory(request.majorCategory())
			.middleCategory(request.middleCategory())
			.status(IssueStatus.PUBLISHED)
			.build());

		// 모바일은 컨테이너 방으로 입장한다. 기존 POST /api/v1/issue 는 컨테이너를 만들지 않아
		// 새 이슈의 containerRoomId 가 null 이 된다. 여기서는 처음부터 같이 만든다.
		ChatRoom container = createContainer(issue);

		record(Action.ISSUE_CREATE, ISSUE, issue.getId(), null,
			Map.of("issueId", issue.getId(), "containerRoomId", container.getId(), "title", issue.getTitle()),
			request.reason(), request.proposalId(), decidedBy, request.approver());

		log.info("온톨로지 이슈 신설 issueId={} proposalId={}", issue.getId(), request.proposalId());
		return new IssueCreateResponse(issue.getId(), container.getId(), true);
	}

	// ── 이슈명 변경 ──────────────────────────────────────────────

	@Transactional
	public IssueRenameResponse renameIssue(Long issueId, IssueRenameRequest request, String decidedBy) {

		IssueEntity issue = findIssue(issueId);
		String previous = issue.getTitle();
		if (request.title().equals(previous)) {
			return new IssueRenameResponse(issueId, previous, previous, false);
		}

		issue.setTitle(request.title());
		// v1.3.5 이관 때 컨테이너 방 이름을 이슈명으로 맞췄다. 같이 바꿔야 모바일 방 제목이 따라온다.
		chatRoomRepository.findFirstByIssueEntity_IdAndRoomType(issueId, ChatRoomType.CONTAINER)
			.ifPresent(container -> container.setTitle(request.title()));

		record(Action.ISSUE_RENAME, ISSUE, issueId, Map.of("title", previous), Map.of("title", request.title()),
			request.reason(), null, decidedBy, request.approver());
		return new IssueRenameResponse(issueId, previous, request.title(), true);
	}

	// ── 토론 주제 ────────────────────────────────────────────────

	@Transactional
	public DebateTopicCreateResponse createDebateTopic(Long issueId, DebateTopicCreateRequest request, String decidedBy) {

		Optional<AdminChangeLog> done = changeLogRepository.findByProposalIdAndAction(request.proposalId(), Action.TOPIC_CREATE);
		if (done.isPresent()) {
			JsonNode after = readJson(done.get().getAfterValue());
			return new DebateTopicCreateResponse(after.path("chatRoomId").asLong(), after.path("containerRoomId").asLong(), false);
		}

		IssueEntity issue = findIssue(issueId);
		ChatRoom container = chatRoomRepository.findFirstByIssueEntity_IdAndRoomType(issueId, ChatRoomType.CONTAINER)
			.orElseGet(() -> createContainer(issue));

		ChatRoom thread = chatRoomRepository.save(ChatRoom.builder()
			.issueEntity(issue)
			.title(request.title())
			.content(request.content())
			.roomType(ChatRoomType.THREAD)
			.containerRoomId(container.getId())
			.status(ChatRoomStatus.OPEN)
			.build());

		record(Action.TOPIC_CREATE, CHAT_ROOM, thread.getId(), null,
			Map.of("chatRoomId", thread.getId(), "containerRoomId", container.getId(), "issueId", issueId, "title", request.title()),
			request.reason(), request.proposalId(), decidedBy, request.approver());

		log.info("온톨로지 토론 주제 신설 issueId={} chatRoomId={} proposalId={}", issueId, thread.getId(), request.proposalId());
		return new DebateTopicCreateResponse(thread.getId(), container.getId(), true);
	}

	@Transactional
	public TopicStatusChangeResponse changeTopicStatus(Long chatRoomId, TopicStatusChangeRequest request, String decidedBy) {

		ChatRoom room = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHATROOM));

		// 컨테이너를 숨기면 이슈의 채팅 전체가 사라진다. 토론 주제(스레드·레거시 방)만 다룬다.
		ChatRoomStatus current = room.getStatus();
		if (room.getRoomType() == ChatRoomType.CONTAINER
			|| !TOPIC_SWITCHABLE.contains(current) || !TOPIC_SWITCHABLE.contains(request.status())) {
			throw new CustomException(ErrorCode.INVALID_CHATROOM_STATUS_TRANSITION);
		}
		if (current == request.status()) {
			return new TopicStatusChangeResponse(chatRoomId, current, current, false);
		}

		room.setStatus(request.status());
		record(Action.TOPIC_STATUS, CHAT_ROOM, chatRoomId, Map.of("status", current.name()),
			Map.of("status", request.status().name()), request.reason(), null, decidedBy, request.approver());
		return new TopicStatusChangeResponse(chatRoomId, current, request.status(), true);
	}

	// ── 사건 ────────────────────────────────────────────────────

	@Transactional
	public EventLinkResponse linkEvents(Long issueId, EventLinkRequest request, String decidedBy) {

		Optional<AdminChangeLog> done = changeLogRepository.findByProposalIdAndAction(request.proposalId(), Action.EVENT_LINK);
		if (done.isPresent()) {
			List<Long> eventIds = new ArrayList<>();
			readJson(done.get().getAfterValue()).path("eventIds").forEach(id -> eventIds.add(id.asLong()));
			return new EventLinkResponse(eventIds, false);
		}

		findIssue(issueId);
		verifyMediaExists(request.events());

		LocalDateTime now = LocalDateTime.now();
		List<Long> eventIds = new ArrayList<>();
		for (EventLinkRequest.Event item : request.events()) {
			NewsEvent event = newsEventRepository.save(NewsEvent.builder()
				.name(item.name())
				.occurredOn(item.date())
				.majorCategory(item.major())
				.middleCategory(item.middle())
				.createdAt(now)
				.build());
			eventIds.add(event.getId());

			issueNewsEventRepository.save(IssueNewsEvent.builder()
				.issueId(issueId)
				.eventId(event.getId())
				.approvedBy(request.approver())
				.createdAt(now)
				.build());

			if (item.mediaIds() != null) {
				for (Long mediaId : new LinkedHashSet<>(item.mediaIds())) {
					newsEventMediaRepository.save(NewsEventMedia.builder().eventId(event.getId()).mediaId(mediaId).build());
				}
			}
		}

		record(Action.EVENT_LINK, ISSUE, issueId, null, Map.of("eventIds", eventIds),
			request.reason(), request.proposalId(), decidedBy, request.approver());
		return new EventLinkResponse(eventIds, true);
	}

	@Transactional
	public EventUnlinkResponse unlinkEvents(Long issueId, EventUnlinkRequest request, String decidedBy) {

		findIssue(issueId);
		List<IssueNewsEvent> links = issueNewsEventRepository.findByIssueIdAndEventIdIn(issueId, request.eventIds());
		if (links.isEmpty()) {
			return new EventUnlinkResponse(List.of());
		}

		List<Long> unlinked = links.stream().map(IssueNewsEvent::getEventId).toList();
		issueNewsEventRepository.deleteAll(links);

		record(Action.EVENT_UNLINK, ISSUE, issueId, Map.of("eventIds", unlinked), null,
			request.reason(), null, decidedBy, request.approver());
		return new EventUnlinkResponse(unlinked);
	}

	// ── 내부 ────────────────────────────────────────────────────

	private IssueEntity findIssue(Long issueId) {
		return issueJpaRepository.findById(issueId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ISSUE));
	}

	private ChatRoom createContainer(IssueEntity issue) {
		return chatRoomRepository.save(ChatRoom.builder()
			.issueEntity(issue)
			.title(issue.getTitle())
			.roomType(ChatRoomType.CONTAINER)
			.status(ChatRoomStatus.OPEN)
			.build());
	}

	private void verifyMediaExists(List<EventLinkRequest.Event> events) {
		Set<Long> requested = new LinkedHashSet<>();
		events.stream().filter(e -> e.mediaIds() != null).forEach(e -> requested.addAll(e.mediaIds()));
		if (requested.isEmpty()) {
			return;
		}
		Set<Long> found = new LinkedHashSet<>();
		mediaJpaRepository.findAllById(requested).forEach(media -> found.add(media.getId()));
		if (found.size() != requested.size()) {
			requested.removeAll(found);
			throw new CustomException(ErrorCode.MEDIA_NOT_FOUND, "존재하지 않는 media id: " + requested);
		}
	}

	private void record(Action action, String entityType, Long entityId, Object before, Object after,
		String reason, String proposalId, String decidedBy, String approver) {

		changeLogRepository.save(AdminChangeLog.builder()
			.action(action)
			.entityType(entityType)
			.entityId(entityId)
			.beforeValue(writeJson(before))
			.afterValue(writeJson(after))
			.reason(reason)
			.proposalId(proposalId)
			.decidedBy(decidedBy)
			.approver(approver)
			.decidedAt(LocalDateTime.now())
			.build());
	}

	private String writeJson(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("변경 이력 직렬화 실패", e);
		}
	}

	private JsonNode readJson(String value) {
		try {
			return objectMapper.readTree(value == null ? "{}" : value);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("변경 이력 역직렬화 실패", e);
		}
	}
}
