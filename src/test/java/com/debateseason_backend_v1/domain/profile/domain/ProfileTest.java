package com.debateseason_backend_v1.domain.profile.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProfileTest {

	@Test
	@DisplayName("프로필을 생성할 수 있다")
	void createProfile() {
		// given
		Long userId = 1L;
		Long communityId = 1L;
		String nickname = "토론왕";
		GenderType gender = GenderType.MALE;
		AgeRangeType ageRange = AgeRangeType.TWENTIES;
		Region residence = Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN);
		Region hometown = Region.of(ProvinceType.BUSAN, DistrictType.JUNG_BUSAN);

		// when & then
		assertThatCode(() -> {
			Profile.create(userId, communityId, nickname, gender, ageRange, residence, hometown);
		}).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("프로필을 업데이트할 수 있다")
	void updateProfile() {
		// given
		Profile profile = Profile.create(
			1L, 1L, "토론왕", GenderType.MALE, AgeRangeType.TWENTIES,
			Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN),
			Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN)
		);

		String newNickname = "토론마스터";
		GenderType newGender = GenderType.FEMALE;
		AgeRangeType newAgeRange = AgeRangeType.THIRTIES;
		Region newResidence = Region.of(ProvinceType.BUSAN, DistrictType.JUNG_BUSAN);
		Region newHometown = Region.of(ProvinceType.BUSAN, DistrictType.JUNG_BUSAN);

		// when & then
		assertThatCode(() -> {
			profile.update(newNickname, newGender, newAgeRange, newResidence, newHometown);
		}).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("프로필을 익명화할 수 있다")
	void anonymizeProfile() {
		// given
		Profile profile = Profile.create(
			1L, 1L, "토론왕", GenderType.MALE, AgeRangeType.TWENTIES,
			Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN),
			Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN)
		);

		String uuid = "abc123";

		// when & then
		assertThatCode(() -> {
			profile.anonymize(uuid);
		}).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("프로필 이미지를 업데이트할 수 있다")
	void updateProfileImage() {
		// given
		Profile profile = Profile.create(
			1L, 1L, "토론왕", GenderType.MALE, AgeRangeType.TWENTIES,
			Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN),
			Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN)
		);

		String newProfileImage = "BLUE";

		// when & then
		assertThatCode(() -> {
			profile.updateProfileImage(newProfileImage);
		}).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("커뮤니티 타입을 가져올 수 있다")
	void getCommunityType() {
		// given
		Long communityId = 1L; // DC_INSIDE
		Profile profile = Profile.create(
			1L, communityId, "토론왕", GenderType.MALE, AgeRangeType.TWENTIES,
			Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN),
			Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN)
		);

		// when
		CommunityType communityType = profile.getCommunityType();

		// then
		assertThat(communityType).isEqualTo(CommunityType.DC_INSIDE);
		assertThat(communityType.getId()).isEqualTo(communityId);
		assertThat(communityType.getName()).isEqualTo("디시인사이드");
	}

	@Test
	@DisplayName("communityId가 null이면 getCommunityType도 null을 반환한다")
	void getCommunityTypeReturnsNullForNullCommunityId() {
		// given
		Profile profile = Profile.create(
			1L, null, "토론왕", GenderType.MALE, AgeRangeType.TWENTIES,
			Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN),
			Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN)
		);

		// when
		CommunityType communityType = profile.getCommunityType();

		// then
		assertThat(communityType).isNull();
	}

	@Test
	@DisplayName("잘못된 닉네임으로 프로필을 생성하면 예외가 발생한다")
	void createProfileWithInvalidNickname() {
		// given
		Long userId = 1L;
		Long communityId = 1L;
		String invalidNickname = ""; // 빈 문자열
		GenderType gender = GenderType.MALE;
		AgeRangeType ageRange = AgeRangeType.TWENTIES;
		Region residence = Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN);
		Region hometown = Region.of(ProvinceType.BUSAN, DistrictType.JUNG_BUSAN);

		// when & then
		assertThatThrownBy(() -> {
			Profile.create(userId, communityId, invalidNickname, gender, ageRange, residence, hometown);
		}).isInstanceOf(Exception.class);
	}

	@Test
	@DisplayName("잘못된 닉네임으로 프로필을 업데이트하면 예외가 발생한다")
	void updateProfileWithInvalidNickname() {
		// given
		Profile profile = Profile.create(
			1L, 1L, "토론왕", GenderType.MALE, AgeRangeType.TWENTIES,
			Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN),
			Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN)
		);

		String invalidNickname = ""; // 빈 문자열
		GenderType newGender = GenderType.FEMALE;
		AgeRangeType newAgeRange = AgeRangeType.THIRTIES;
		Region newResidence = Region.of(ProvinceType.BUSAN, DistrictType.JUNG_BUSAN);
		Region newHometown = Region.of(ProvinceType.BUSAN, DistrictType.JUNG_BUSAN);

		// when & then
		assertThatThrownBy(() -> {
			profile.update(invalidNickname, newGender, newAgeRange, newResidence, newHometown);
		}).isInstanceOf(Exception.class);
	}

	@Test
	@DisplayName("잘못된 지역 관계로 프로필을 생성하면 예외가 발생한다")
	void createProfileWithInvalidRegion() {
		// given
		Long userId = 1L;
		Long communityId = 1L;
		String nickname = "토론왕";
		GenderType gender = GenderType.MALE;
		AgeRangeType ageRange = AgeRangeType.TWENTIES;

		// when & then
		assertThatThrownBy(() -> {
			Region invalidRegion = Region.of(ProvinceType.SEOUL, DistrictType.JUNG_BUSAN); // 서울-부산 중구 (잘못된 관계)
			Profile.create(userId, communityId, nickname, gender, ageRange, invalidRegion, invalidRegion);
		}).isInstanceOf(Exception.class);
	}
}