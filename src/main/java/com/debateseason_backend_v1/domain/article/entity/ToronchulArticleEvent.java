package com.debateseason_backend_v1.domain.article.entity;

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

/** 아티클 ↔ news_event_id. (article_id, news_event_id) 유일. */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "toronchul_article_event")
public class ToronchulArticleEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "article_id", nullable = false)
	private Long articleId;

	@Column(name = "news_event_id", nullable = false)
	private Long newsEventId;
}
