package com.debateseason_backend_v1.domain.article.entity;

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
 * 아티클 수정 전 내용 (append-only). 수정할 때마다 바뀌기 전의 편집 가능한 필드를 통째로 남긴다.
 * 되돌리기(POST /admin/change-log/{id}/revert)가 이 행으로 복원한다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "toronchul_article_revision")
public class ToronchulArticleRevision {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "article_id", nullable = false)
	private Long articleId;

	@Column(name = "headline", nullable = false)
	private String headline;

	@Column(name = "summary", columnDefinition = "TEXT")
	private String summary;

	@Column(name = "body", nullable = false, columnDefinition = "TEXT")
	private String body;

	@Column(name = "keywords", columnDefinition = "TEXT")
	private String keywords;

	@Column(name = "article_section", length = 32)
	private String articleSection;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
}
