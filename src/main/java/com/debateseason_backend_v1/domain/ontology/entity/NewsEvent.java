package com.debateseason_backend_v1.domain.ontology.entity;

import java.time.LocalDate;
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
import lombok.Setter;

/**
 * 온톨로지의 사건. 이슈 ⊃ 사건 은 M:N 이라 연결은 {@link IssueNewsEvent} 가 맡는다.
 *
 * 아직 앱에 보여 줄 화면이 없어 저장만 한다. 테이블명을 event 로 하지 않은 건
 * MariaDB 의 EVENT(스케줄러) 키워드·앱 이벤트와 헷갈리지 않게 하려는 것이다.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "news_event")
public class NewsEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "occurred_on")
	private LocalDate occurredOn;

	@Column(name = "major_category", length = 64)
	private String majorCategory;

	@Column(name = "middle_category", length = 64)
	private String middleCategory;

	// 병합(타임라인 개정)으로 흡수된 사건이면 흡수한 사건 id. 행은 지우지 않는다.
	@Column(name = "merged_into")
	private Long mergedInto;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
}
