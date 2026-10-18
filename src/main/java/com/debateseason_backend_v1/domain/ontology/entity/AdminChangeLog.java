package com.debateseason_backend_v1.domain.ontology.entity;

import java.time.LocalDateTime;

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
 * 관리 API 로 바꾼 것의 이력 (append-only). 롤백은 이 기록의 before 를 보고 되돌린다.
 *
 * 이슈 상태(잠재우기·깨우기·롤백)만은 issue_status_history 에 따로 남는다 — 먼저 생긴 테이블이라 그대로 둔다.
 *
 * {@code (proposal_id, action)} 이 유일하다. 파이프라인이 같은 제안의 반영을 재시도해도 두 번 만들지 않는 근거다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "admin_change_log")
public class AdminChangeLog {

	public enum Action {
		ISSUE_CREATE,
		ISSUE_RENAME,
		TOPIC_CREATE,
		TOPIC_STATUS,
		EVENT_LINK,
		EVENT_UNLINK,
		EVENT_UPDATE,
		EVENT_MERGE,
		// 다른 이력 1건을 되돌린 기록. proposal_id = "revert:{원본 id}" 라 같은 되돌리기는 한 번만 일어난다.
		REVERT
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "action", nullable = false, length = 32)
	private Action action;

	// ISSUE / CHAT_ROOM / NEWS_EVENT / CHANGE_LOG
	@Column(name = "entity_type", nullable = false, length = 32)
	private String entityType;

	@Column(name = "entity_id", nullable = false)
	private Long entityId;

	// 바뀌기 전·후 값 (JSON 문자열). 생성은 before 가 비어 있다.
	@Column(name = "before_value", columnDefinition = "TEXT")
	private String beforeValue;

	@Column(name = "after_value", columnDefinition = "TEXT")
	private String afterValue;

	@Column(name = "reason", length = 255)
	private String reason;

	// 파이프라인의 제안 ID. 생성 계열은 필수, 나머지는 비어 있을 수 있다.
	@Column(name = "proposal_id", length = 64)
	private String proposalId;

	@Column(name = "decided_by", nullable = false, length = 64)
	private String decidedBy;

	@Column(name = "approver", length = 64)
	private String approver;

	@Column(name = "decided_at", nullable = false)
	private LocalDateTime decidedAt;
}
