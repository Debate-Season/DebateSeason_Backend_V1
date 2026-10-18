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

/**
 * 이슈 타임라인의 버전. 사건 추가·해제·수정·병합·되돌리기가 일어날 때마다 1씩 오른다.
 * snapshot 은 그 시점에 이슈에 연결된 사건 목록(JSON 배열: id·name·date), 날짜순이다.
 * (issue_id, version) 유일.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "issue_timeline_version")
public class IssueTimelineVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "issue_id", nullable = false)
	private Long issueId;

	@Column(name = "version", nullable = false)
	private Integer version;

	// 이 버전을 만든 변경 (admin_change_log.id)
	@Column(name = "change_log_id", nullable = false)
	private Long changeLogId;

	@Column(name = "snapshot", nullable = false, columnDefinition = "TEXT")
	private String snapshot;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
}
