package com.debateseason_backend_v1.domain.partner.entity;

import java.io.Serializable;
import java.time.LocalDate;

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

/** 고객사 호출 미터링 (계약 과금 근거). 고객사·일자(KST)·엔드포인트별 호출 수. */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "partner_usage_daily")
public class PartnerUsageDaily {

	@EmbeddedId
	private Key id;

	@Column(name = "calls", nullable = false)
	private long calls;

	@Embeddable
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@EqualsAndHashCode
	public static class Key implements Serializable {
		@Column(name = "partner", length = 32)
		private String partner;
		@Column(name = "usage_date")
		private LocalDate usageDate;
		@Column(name = "endpoint", length = 32)
		private String endpoint;
	}
}
