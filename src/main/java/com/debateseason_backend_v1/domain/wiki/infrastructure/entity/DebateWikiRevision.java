package com.debateseason_backend_v1.domain.wiki.infrastructure.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.wiki.domain.WikiSource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 토론위키 본문 리비전 (v1.4.0). append-only 이력.
 *
 * AI 생성=새 리비전({@code source=AI}, {@code model} 채움), ADMIN 편집=새 리비전({@code source=ADMIN},
 * {@code createdBy} 채움). 롤백은 {@link DebateWiki#getPublishedRevisionId()} 를 옛 리비전으로 되돌리면 된다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "debate_wiki_revision")
@EntityListeners(AuditingEntityListener.class)
public class DebateWikiRevision {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "revision_id")
	private Long id;

	@Column(name = "wiki_id", nullable = false)
	private Long wikiId;

	// 본문(Markdown). 나무위키식 문서라 길 수 있어 LOB(대용량 텍스트). prod DDL 은 MEDIUMTEXT.
	@Lob
	@Column(name = "content", nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "source", nullable = false)
	private WikiSource source;

	// 생성 모델명 (AI 리비전). ADMIN 편집은 NULL.
	@Column(name = "model")
	private String model;

	@Column(name = "edit_summary")
	private String editSummary;

	// ADMIN 편집 시 user_id. AI 생성은 NULL.
	@Column(name = "created_by")
	private Long createdBy;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}
