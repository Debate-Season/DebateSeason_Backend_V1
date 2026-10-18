package com.debateseason_backend_v1.domain.article.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import lombok.Setter;

/**
 * 토론철이 작성·발행하는 아티클. 생성·데스킹은 데이터 레포, 적재·서빙은 백엔드.
 *
 * <p>크롤러가 쓰는 운영 {@code article}(뉴스 본문)과 다른 테이블이다. 이름을 섞지 않는다.
 * <p>JSON 컬럼(body·keywords·sensitivity_axes)은 문자열로 들고, 서비스가 직렬화한다.
 * body = [{text, sources:[mediaId]}] 문단 배열. 문단 sources 는 내부 근거라 공개 응답에 내지 않는다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "toronchul_article")
@EntityListeners(AuditingEntityListener.class)
public class ToronchulArticle {

	public static final String TORONCHUL_EDITION = "toronchul";
	public static final String DEFAULT_BYLINE = "토론철 AI 기자";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	// 데이터 쪽 멱등 키. 예: 2026-10-18/toronchul/20261018-019
	@Column(name = "article_key", nullable = false, unique = true, length = 80)
	private String articleKey;

	// toronchul | tmoney
	@Column(name = "edition", nullable = false, length = 16)
	private String edition;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 16)
	private ArticleStatus status;

	@Column(name = "headline", nullable = false)
	private String headline;

	@Column(name = "summary", columnDefinition = "TEXT")
	private String summary;

	@Column(name = "body", nullable = false, columnDefinition = "TEXT")
	private String body;

	@Column(name = "article_section", length = 32)
	private String articleSection;

	@Column(name = "keywords", columnDefinition = "TEXT")
	private String keywords;

	@Column(name = "debate_point")
	private String debatePoint;

	@Column(name = "byline", nullable = false, length = 32)
	private String byline;

	@Column(name = "ai_generated", nullable = false)
	private boolean aiGenerated;

	@Column(name = "view_count", nullable = false)
	private int viewCount;

	@Column(name = "image_url", length = 512)
	private String imageUrl;

	@Column(name = "sensitivity_max", precision = 4, scale = 3)
	private BigDecimal sensitivityMax;

	@Column(name = "sensitivity_axes", columnDefinition = "TEXT")
	private String sensitivityAxes;

	@Column(name = "generator_version", length = 80)
	private String generatorVersion;

	// 데이터 쪽에서 기사를 만든 시각 (요청값). 없으면 적재 시각.
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "reviewed_at")
	private LocalDateTime reviewedAt;

	@Column(name = "reviewed_by", length = 64)
	private String reviewedBy;

	// 처음 PUBLISHED 가 된 시각 = 공개 API 의 작성일시. 다시 게시해도 바꾸지 않는다.
	@Column(name = "date_published")
	private LocalDateTime datePublished;
}
