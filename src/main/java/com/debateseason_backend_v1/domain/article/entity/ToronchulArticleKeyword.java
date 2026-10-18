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

/**
 * 아티클 연관 키워드 색인. toronchul_article.keywords(JSON) 를 정확 일치 검색용으로 펼친 것.
 * 고객사 키워드 뉴스 조회가 이 표를 (keyword, article_id) 인덱스로 찾는다. 적재 때 백엔드가 채운다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "toronchul_article_keyword")
public class ToronchulArticleKeyword {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "article_id", nullable = false)
	private Long articleId;

	@Column(name = "keyword", nullable = false, length = 64)
	private String keyword;
}
