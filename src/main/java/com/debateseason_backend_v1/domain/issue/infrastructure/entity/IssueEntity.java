package com.debateseason_backend_v1.domain.issue.infrastructure.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.issue.model.IssueStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "issue")
@Entity
@EntityListeners(AuditingEntityListener.class) // 이걸 붙여야 @CreatedDate가 활성화되서 자동으로 날짜 입력
public class IssueEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "issue_id")
	private Long id;

	private String title;

	@Column(name = "major_category")
	private String majorCategory;

	private String middleCategory;

	// 레거시 행은 NULL(= 시스템/수동 생성). 소유권 판정 시 NULL 이면 ADMIN 만 수정 가능.
	@Column(name = "created_by")
	private Long createdBy;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private IssueStatus status;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// @Builder 는 필드 초기화식을 무시하므로, 생성 경로마다 status 를 빠뜨릴 수 있다.
	// 엔티티가 스스로 기본값을 보장한다.
	@PrePersist
	private void applyDefaultStatus() {
		if (status == null) {
			status = IssueStatus.PUBLISHED;
		}
	}

}
