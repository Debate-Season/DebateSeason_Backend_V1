package com.debateseason_backend_v1.domain.issue.application.service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueStatusHistory;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueStatusHistoryRepository;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;
import com.debateseason_backend_v1.domain.issue.model.response.IssueStatusChangeResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이슈 잠재우기·깨우기, 신설 롤백.
 *
 * PUBLISHED·DORMANT·ARCHIVED 사이를 오간다.
 * <ul>
 *   <li>PUBLISHED ↔ DORMANT — 잠재우기·깨우기 (노출 동일, 분류만 다름)</li>
 *   <li>→ ARCHIVED — 온톨로지가 신설한 이슈의 롤백 (미노출, 데이터 보존)</li>
 *   <li>ARCHIVED → — 롤백을 다시 되돌릴 때</li>
 * </ul>
 * DRAFT 는 쓰는 경로가 없어 막아 둔다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueStatusService {

	private static final Set<IssueStatus> SWITCHABLE = EnumSet.of(IssueStatus.PUBLISHED, IssueStatus.DORMANT, IssueStatus.ARCHIVED);

	private final IssueJpaRepository issueJpaRepository;
	private final IssueStatusHistoryRepository issueStatusHistoryRepository;

	@Transactional
	public IssueStatusChangeResponse changeStatus(
		Long issueId,
		IssueStatus target,
		String reason,
		String approver,
		String decidedBy
	) {
		IssueEntity issue = issueJpaRepository.findById(issueId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ISSUE));

		IssueStatus current = issue.getStatus();
		if (!SWITCHABLE.contains(current) || !SWITCHABLE.contains(target)) {
			throw new CustomException(ErrorCode.INVALID_ISSUE_STATUS_TRANSITION);
		}

		// 파이프라인은 승인 반영을 재시도할 수 있다. 같은 요청이 두 번 와도 이력이 두 줄 생기지 않게 한다.
		if (current == target) {
			return new IssueStatusChangeResponse(issueId, current, target, false);
		}

		issue.setStatus(target);
		issueStatusHistoryRepository.save(IssueStatusHistory.builder()
			.issueId(issueId)
			.fromStatus(current)
			.toStatus(target)
			.reason(reason)
			.decidedBy(decidedBy)
			.approver(approver)
			.decidedAt(LocalDateTime.now())
			.build());

		log.info("이슈 상태 변경 issueId={} {} -> {} decidedBy={}", issueId, current, target, decidedBy);
		return new IssueStatusChangeResponse(issueId, current, target, true);
	}
}
