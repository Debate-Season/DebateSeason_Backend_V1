package com.debateseason_backend_v1.domain.partner.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 고객사별 기사 조회 수 (D5: 고객사별 분리). 토론철 앱·웹 조회와 섞지 않는다. */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "partner_article_view")
public class PartnerArticleView {

	@EmbeddedId
	private Key id;

	@Column(name = "views", nullable = false)
	private long views;

	@Embeddable
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@EqualsAndHashCode
	public static class Key implements Serializable {
		@Column(name = "partner", length = 32)
		private String partner;
		@Column(name = "article_id")
		private Long articleId;
	}
}
