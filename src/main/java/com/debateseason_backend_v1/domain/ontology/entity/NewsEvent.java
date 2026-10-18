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

	// 최초 발생 시각 (KST). 데이터 쪽이 계산한다: 사건 기사의 가장 이른 보도 시각, 앞선 14일 안의 같은 사건 보도 포함.
	// 있으면 occurred_on 은 이 값의 날짜와 같게 맞춘다.
	@Column(name = "occurred_at")
	private LocalDateTime occurredAt;

	@Column(name = "major_category", length = 64)
	private String majorCategory;

	@Column(name = "middle_category", length = 64)
	private String middleCategory;

	// 병합(타임라인 개정)으로 흡수된 사건이면 흡수한 사건 id. 행은 지우지 않는다.
	@Column(name = "merged_into")
	private Long mergedInto;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	/** 타임라인 정렬 시각. 발생 시각이 있으면 그것, 날짜만 있으면 그날 00:00, 둘 다 없으면 null(맨 뒤). */
	public LocalDateTime when() {
		if (occurredAt != null) {
			return occurredAt;
		}
		return occurredOn == null ? null : occurredOn.atStartOfDay();
	}

	/** 발생 시각을 정하면 날짜도 그 날로 맞춘다. 시각 없이 날짜만 오면 날짜만 바꾼다. */
	public void setWhen(LocalDate date, LocalDateTime at) {
		if (at != null) {
			this.occurredAt = at;
			this.occurredOn = at.toLocalDate();
		} else if (date != null) {
			this.occurredOn = date;
			if (this.occurredAt != null && !this.occurredAt.toLocalDate().equals(date)) {
				this.occurredAt = null;
			}
		}
	}
}
