package com.debateseason_backend_v1.domain.ontology.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueStatusHistory;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueStatusHistoryRepository;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;
import com.debateseason_backend_v1.domain.ontology.entity.AdminChangeLog;
import com.debateseason_backend_v1.domain.ontology.entity.AdminChangeLog.Action;
import com.debateseason_backend_v1.domain.ontology.model.request.IssueLaunchRequest;
import com.debateseason_backend_v1.domain.ontology.model.response.IssueLaunchResponse;
import com.debateseason_backend_v1.domain.ontology.repository.AdminChangeLogRepository;
import com.debateseason_backend_v1.domain.ontology.repository.IssueNewsEventRepository;
import com.debateseason_backend_v1.domain.wiki.domain.WikiStatus;
import com.debateseason_backend_v1.domain.wiki.infrastructure.entity.DebateWiki;
import com.debateseason_backend_v1.domain.wiki.infrastructure.entity.DebateWikiRevision;
import com.debateseason_backend_v1.domain.wiki.infrastructure.repository.DebateWikiRepository;
import com.debateseason_backend_v1.domain.wiki.infrastructure.repository.DebateWikiRevisionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이슈 공개 (사용자 결정 2026-10-19): 이슈는 위키·사건 타임라인이 갖춰진 뒤 함께 공개한다.
 *
 * <p>데이터 레포 흐름: 이슈 신설(DRAFT) → 사건 연결 → 위키 DRAFT 업로드 → Discord "이슈 공개" 제안 ✅ → 이 API.
 * Discord ✅ 를 사람 승인으로 본다. 그래서 <b>공개 전(DRAFT) 이슈에 한해</b> 파이프라인 키로 위키 게시가 열린다.
 * 이미 공개된 이슈의 위키 갱신은 여전히 사람이 직접 게시한다 (/admin/wiki/publish 는 wiki-editor 키·ADMIN 만).
 *
 * <p>조건: 이슈 DRAFT, 사건 1건 이상, 위키 리비전이 이 이슈의 것. 셋 중 하나라도 아니면 409 ISSUE_NOT_LAUNCHABLE.
 * 위키 게시와 이슈 공개는 한 트랜잭션이다 — 위키 없이 공개되거나 공개 없이 위키만 게시되는 반쪽 상태가 없다.
 * 롤백은 PATCH /admin/issue/{id}/status DRAFT (위키 게시 포인터는 둔다 — 이슈가 숨으니 보이지 않는다).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueLaunchService {

	private final IssueJpaRepository issueJpaRepository;
	private final IssueStatusHistoryRepository statusHistoryRepository;
	private final IssueNewsEventRepository issueNewsEventRepository;
	private final DebateWikiRepository wikiRepository;
	private final DebateWikiRevisionRepository revisionRepository;
	private final AdminChangeLogRepository changeLogRepository;
	private final ChangeLogWriter changeLog;

	@Transactional
	public IssueLaunchResponse launch(Long issueId, IssueLaunchRequest request, String decidedBy) {

		Optional<AdminChangeLog> done = changeLogRepository.findByProposalIdAndAction(request.proposalId(), Action.ISSUE_LAUNCH);
		if (done.isPresent()) {
			var after = changeLog.read(done.get().getAfterValue());
			return new IssueLaunchResponse(issueId, after.path("wikiRevisionId").asLong(), IssueStatus.PUBLISHED.name(),
				after.path("eventCount").asInt(), false);
		}

		IssueEntity issue = issueJpaRepository.findById(issueId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ISSUE));
		int eventCount = issueNewsEventRepository.findByIssueId(issueId).size();
		Optional<DebateWiki> wiki = wikiRepository.findByIssueId(issueId);
		Optional<DebateWikiRevision> revision = revisionRepository.findById(request.wikiRevisionId());

		if (issue.getStatus() != IssueStatus.DRAFT || eventCount == 0 || wiki.isEmpty() || revision.isEmpty()
			|| !revision.get().getWikiId().equals(wiki.get().getId())) {
			throw new CustomException(ErrorCode.ISSUE_NOT_LAUNCHABLE, reason(issue, eventCount, wiki, revision));
		}

		DebateWiki w = wiki.get();
		Long previousRevision = w.getPublishedRevisionId();
		w.setPublishedRevisionId(request.wikiRevisionId());
		w.setStatus(WikiStatus.PUBLISHED);
		issue.setStatus(IssueStatus.PUBLISHED);

		LocalDateTime now = LocalDateTime.now();
		statusHistoryRepository.save(IssueStatusHistory.builder()
			.issueId(issueId).fromStatus(IssueStatus.DRAFT).toStatus(IssueStatus.PUBLISHED)
			.reason(request.reason() != null ? request.reason() : "launch: 위키·타임라인과 함께 공개")
			.decidedBy(decidedBy).approver(request.approver()).decidedAt(now).build());
		changeLog.record(Action.ISSUE_LAUNCH, "ISSUE", issueId,
			Map.of("status", IssueStatus.DRAFT.name(), "wikiRevisionId", String.valueOf(previousRevision)),
			Map.of("status", IssueStatus.PUBLISHED.name(), "wikiRevisionId", request.wikiRevisionId(), "eventCount", eventCount),
			request.reason(), request.proposalId(), decidedBy, request.approver());

		log.info("이슈 공개 issueId={} wikiRevisionId={} events={} decidedBy={}", issueId, request.wikiRevisionId(), eventCount, decidedBy);
		return new IssueLaunchResponse(issueId, request.wikiRevisionId(), IssueStatus.PUBLISHED.name(), eventCount, true);
	}

	// 409 메시지에 무엇이 모자란지 적는다 — 파이프라인이 사람에게 그대로 보여 준다
	private static String reason(IssueEntity issue, int eventCount, Optional<DebateWiki> wiki, Optional<DebateWikiRevision> revision) {
		if (issue.getStatus() != IssueStatus.DRAFT) {
			return "이슈가 DRAFT 가 아닙니다 (지금 " + issue.getStatus() + ").";
		}
		if (eventCount == 0) {
			return "이슈에 연결된 사건이 없습니다. 타임라인을 먼저 채우세요.";
		}
		if (wiki.isEmpty() || revision.isEmpty()) {
			return "위키 리비전이 없습니다. 위키 DRAFT 를 먼저 올리세요.";
		}
		return "위키 리비전이 이 이슈의 것이 아닙니다.";
	}
}
