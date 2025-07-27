package com.debateseason_backend_v1.domain.profile.domain;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

	@Enumerated(EnumType.STRING)
	private ProvinceType provinceType;

	@Enumerated(EnumType.STRING)
	private DistrictType districtType;

	public static Region of(ProvinceType provinceType, DistrictType districtType) {

		if (districtType.getProvinceType() != provinceType) {
			throw new CustomException(ErrorCode.INVALID_DISTRICT_PROVINCE_RELATION);
		}

		return Region.builder()
			.provinceType(provinceType)
			.districtType(districtType)
			.build();
	}

	public static Region anonymize() {
		return Region.builder()
			.provinceType(ProvinceType.UNDEFINED)
			.districtType(DistrictType.UNDEFINED)
			.build();
	}
}
