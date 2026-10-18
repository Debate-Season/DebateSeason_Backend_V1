package com.debateseason_backend_v1.domain.issue.infrastructure.entity;

import java.time.LocalDateTime;

import com.debateseason_backend_v1.domain.issue.model.IssueStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이슈 상태 전이 이력. 한 번 쓰면 고치지 않는다 (append-only).
 *
 * issue.status 는 "지금" 만 담는다. 누가 언제 왜 바꿨는지는 여기서만 알 수 있다.
 * 잠재우기는 Discord 승인 흐름을 거치므로, 요청 주체(decidedBy)와 승인자(approver)를 따로 남긴다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "issue_status_history")
public class IssueStatusHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "issue_id", nullable = false)
	private Long issueId;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", nullable = false, length = 20)
	private IssueStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, length = 20)
	private IssueStatus toStatus;

	@Column(name = "reason", length = 255)
	private String reason;

	// 요청 주체. "service:ontology" (서비스 API 키) 또는 "user:{userId}" (ADMIN JWT).
	@Column(name = "decided_by", nullable = false, length = 64)
	private String decidedBy;

	// 승인한 사람. 파이프라인 경로는 Discord 사용자 ID, ADMIN 직접 호출은 비어 있을 수 있다.
	@Column(name = "approver", length = 64)
	private String approver;

	@Column(name = "decided_at", nullable = false)
	private LocalDateTime decidedAt;
}
