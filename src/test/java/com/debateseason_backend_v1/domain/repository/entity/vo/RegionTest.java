package com.debateseason_backend_v1.domain.repository.entity.vo;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.profile.enums.DistrictType;
import com.debateseason_backend_v1.domain.profile.enums.ProvinceType;

class RegionTest {

	@Test
	@DisplayName("지역 정보를 생성할 수 있다")
	void createRegion() {
		// given
		ProvinceType provinceType = ProvinceType.SEOUL;
		DistrictType districtType = DistrictType.GANGNAM;

		// when
		Region region = Region.of(provinceType, districtType);

		// then
		assertThat(region.getProvinceType()).isEqualTo(provinceType);
		assertThat(region.getDistrictType()).isEqualTo(districtType);
	}

	@Test
	@DisplayName("시도와 시군구가 일치하지 않으면 예외가 발생한다")
	void throwExceptionWhenDistrictProvinceDoNotMatch() {
		// given
		ProvinceType provinceType = ProvinceType.SEOUL;
		DistrictType districtType = DistrictType.HAEUNDAE; // 부산 해운대구

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			Region.of(provinceType, districtType);
		});

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_DISTRICT_PROVINCE_RELATION);
	}

	@Test
	@DisplayName("시도가 null이면 예외가 발생한다")
	void throwExceptionWhenProvinceIsNull() {
		// given
		ProvinceType provinceType = null;
		DistrictType districtType = DistrictType.GANGNAM;

		// when & then
		assertThrows(IllegalArgumentException.class, () -> {
			Region.of(provinceType, districtType);
		});
	}

	@Test
	@DisplayName("시군구가 null이면 예외가 발생한다")
	void throwExceptionWhenDistrictIsNull() {
		// given
		ProvinceType provinceType = ProvinceType.SEOUL;
		DistrictType districtType = null;

		// when & then
		assertThrows(IllegalArgumentException.class, () -> {
			Region.of(provinceType, districtType);
		});
	}

	@Test
	@DisplayName("동일한 값을 가진 Region 객체는 equals 비교에서 true를 반환한다")
	void equalRegionsReturnTrueForEqualsComparison() {
		// given
		Region region1 = Region.of(ProvinceType.SEOUL, DistrictType.GANGNAM);
		Region region2 = Region.of(ProvinceType.SEOUL, DistrictType.GANGNAM);

		// when & then
		assertThat(region1).isEqualTo(region2);
		assertThat(region1.hashCode()).isEqualTo(region2.hashCode());
	}

	@Test
	@DisplayName("다른 값을 가진 Region 객체는 equals 비교에서 false를 반환한다")
	void differentRegionsReturnFalseForEqualsComparison() {
		// given
		Region region1 = Region.of(ProvinceType.SEOUL, DistrictType.GANGNAM);
		Region region2 = Region.of(ProvinceType.SEOUL, DistrictType.SEOCHO);
		Region region3 = Region.of(ProvinceType.BUSAN, DistrictType.HAEUNDAE);

		// when & then
		assertThat(region1).isNotEqualTo(region2);
		assertThat(region1).isNotEqualTo(region3);
		assertThat(region2).isNotEqualTo(region3);
	}
}