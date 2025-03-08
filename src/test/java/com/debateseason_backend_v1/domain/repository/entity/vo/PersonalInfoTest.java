package com.debateseason_backend_v1.domain.repository.entity.vo;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.DistrictType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;
import com.debateseason_backend_v1.domain.profile.enums.ProvinceType;

class PersonalInfoTest {

	@Test
	@DisplayName("개인정보를 생성할 수 있다")
	void createPersonalInfo() {
		// given
		String nickname = "토론왕";
		GenderType gender = GenderType.MALE;
		AgeRangeType ageRange = AgeRangeType.TWENTIES;
		Region residence = Region.of(ProvinceType.SEOUL, DistrictType.GANGNAM);
		Region hometown = Region.of(ProvinceType.BUSAN, DistrictType.HAEUNDAE);

		// when
		PersonalInfo personalInfo = PersonalInfo.of(nickname, gender, ageRange, residence, hometown);

		// then
		assertThat(personalInfo.getNickname()).isEqualTo(nickname);
		assertThat(personalInfo.getGender()).isEqualTo(gender);
		assertThat(personalInfo.getAgeRange()).isEqualTo(ageRange);
		assertThat(personalInfo.getResidence()).isEqualTo(residence);
		assertThat(personalInfo.getHometown()).isEqualTo(hometown);
	}

	@Test
	@DisplayName("동일한 값을 가진 PersonalInfo 객체는 equals 비교에서 true를 반환한다")
	void equalPersonalInfosReturnTrueForEqualsComparison() {
		// given
		String nickname = "토론왕";
		GenderType gender = GenderType.MALE;
		AgeRangeType ageRange = AgeRangeType.TWENTIES;
		Region residence = Region.of(ProvinceType.SEOUL, DistrictType.GANGNAM);
		Region hometown = Region.of(ProvinceType.BUSAN, DistrictType.HAEUNDAE);

		PersonalInfo personalInfo1 = PersonalInfo.of(nickname, gender, ageRange, residence, hometown);
		PersonalInfo personalInfo2 = PersonalInfo.of(nickname, gender, ageRange, residence, hometown);

		// when & then
		assertThat(personalInfo1).isEqualTo(personalInfo2);
		assertThat(personalInfo1.hashCode()).isEqualTo(personalInfo2.hashCode());
	}

	@Test
	@DisplayName("다른 값을 가진 PersonalInfo 객체는 equals 비교에서 false를 반환한다")
	void differentPersonalInfosReturnFalseForEqualsComparison() {
		// given
		String nickname1 = "토론왕";
		String nickname2 = "토론마스터";
		GenderType gender1 = GenderType.MALE;
		GenderType gender2 = GenderType.FEMALE;
		AgeRangeType ageRange1 = AgeRangeType.TWENTIES;
		AgeRangeType ageRange2 = AgeRangeType.THIRTIES;
		Region residence1 = Region.of(ProvinceType.SEOUL, DistrictType.GANGNAM);
		Region residence2 = Region.of(ProvinceType.SEOUL, DistrictType.SEOCHO);
		Region hometown1 = Region.of(ProvinceType.BUSAN, DistrictType.HAEUNDAE);
		Region hometown2 = Region.of(ProvinceType.DAEGU, DistrictType.SUSEONG);

		PersonalInfo personalInfo1 = PersonalInfo.of(nickname1, gender1, ageRange1, residence1, hometown1);
		PersonalInfo personalInfo2 = PersonalInfo.of(nickname2, gender1, ageRange1, residence1, hometown1);
		PersonalInfo personalInfo3 = PersonalInfo.of(nickname1, gender2, ageRange1, residence1, hometown1);
		PersonalInfo personalInfo4 = PersonalInfo.of(nickname1, gender1, ageRange2, residence1, hometown1);
		PersonalInfo personalInfo5 = PersonalInfo.of(nickname1, gender1, ageRange1, residence2, hometown1);
		PersonalInfo personalInfo6 = PersonalInfo.of(nickname1, gender1, ageRange1, residence1, hometown2);

		// when & then
		assertThat(personalInfo1).isNotEqualTo(personalInfo2);
		assertThat(personalInfo1).isNotEqualTo(personalInfo3);
		assertThat(personalInfo1).isNotEqualTo(personalInfo4);
		assertThat(personalInfo1).isNotEqualTo(personalInfo5);
		assertThat(personalInfo1).isNotEqualTo(personalInfo6);
	}

	@Test
	@DisplayName("필수 값이 null이면 예외가 발생한다")
	void throwExceptionWhenRequiredFieldsAreNull() {
		// given
		String nickname = "토론왕";
		GenderType gender = GenderType.MALE;
		AgeRangeType ageRange = AgeRangeType.TWENTIES;
		Region residence = Region.of(ProvinceType.SEOUL, DistrictType.GANGNAM);
		Region hometown = Region.of(ProvinceType.BUSAN, DistrictType.HAEUNDAE);

		// when & then
		assertThrows(IllegalArgumentException.class, () -> {
			PersonalInfo.of(null, gender, ageRange, residence, hometown);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			PersonalInfo.of("", gender, ageRange, residence, hometown);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			PersonalInfo.of("  ", gender, ageRange, residence, hometown);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			PersonalInfo.of(nickname, null, ageRange, residence, hometown);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			PersonalInfo.of(nickname, gender, null, residence, hometown);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			PersonalInfo.of(nickname, gender, ageRange, null, hometown);
		});

		assertThrows(IllegalArgumentException.class, () -> {
			PersonalInfo.of(nickname, gender, ageRange, residence, null);
		});
	}
}