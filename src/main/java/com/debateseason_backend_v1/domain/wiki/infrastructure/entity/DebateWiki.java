package com.debateseason_backend_v1.domain.wiki.infrastructure.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.wiki.domain.WikiStatus;

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

/**
 * 토론위키 메타 (v1.4.0). 이슈당 1장.
 *
 * 본문은 여기 두지 않고 {@link DebateWikiRevision} 이력으로 분리한다 — 위키의 본질은 개정 이력이라,
 * AI 재생성·ADMIN 편집·롤백을 모두 리비전 추가/포인터 이동으로 안전하게 처리한다.
 * 게시 상태의 단일 진실은 {@code status} + {@code publishedRevisionId} 조합이다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "debate_wiki")
@EntityListeners(AuditingEntityListener.class)
public class DebateWiki {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "wiki_id")
	private Long id;

	// 이슈 1:1 (DB UNIQUE). 엔티티 조인 대신 id 로 느슨하게 참조.
	@Column(name = "issue_id", nullable = false, unique = true)
	private Long issueId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private WikiStatus status;

	// 현재 게시 중인 리비전. NULL = 미게시.
	@Column(name = "published_revision_id")
	private Long publishedRevisionId;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// @Builder 는 필드 초기화식을 무시하므로 생성 경로마다 status 를 빠뜨릴 수 있다.
	@PrePersist
	private void applyDefaultStatus() {
		if (status == null) {
			status = WikiStatus.DRAFT;
		}
	}
}
