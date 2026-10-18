package com.debateseason_backend_v1.domain.ontology.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 이슈 ↔ 사건 연결. (issue_id, event_id) 유일. 롤백은 이 행만 지우고 사건 행은 남긴다. */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "issue_news_event")
public class IssueNewsEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "issue_id", nullable = false)
	private Long issueId;

	@Column(name = "event_id", nullable = false)
	private Long eventId;

	@Column(name = "approved_by", length = 64)
	private String approvedBy;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
}
