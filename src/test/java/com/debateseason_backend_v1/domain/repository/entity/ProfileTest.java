package com.debateseason_backend_v1.domain.repository.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.profile.enums.DistrictType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;
import com.debateseason_backend_v1.domain.profile.enums.ProvinceType;
import com.debateseason_backend_v1.domain.repository.entity.vo.PersonalInfo;
import com.debateseason_backend_v1.domain.repository.entity.vo.Region;

class ProfileTest {

	@Test
	@DisplayName("프로필을 생성할 수 있다")
	void createProfile() {
		// given
		Long userId = 1L;
		Long communityId = 1L;
		String profileImage = "profile-image-url";

		String nickname = "토론왕";
		GenderType gender = GenderType.MALE;
		AgeRangeType ageRange = AgeRangeType.TWENTIES;
		Region residence = Region.of(ProvinceType.SEOUL, DistrictType.GANGNAM);
		Region hometown = Region.of(ProvinceType.BUSAN, DistrictType.HAEUNDAE);
		PersonalInfo personalInfo = PersonalInfo.of(nickname, gender, ageRange, residence, hometown);

		// when
		Profile profile = Profile.create(userId, communityId, profileImage, personalInfo);

		// then
		assertThat(profile.getUserId()).isEqualTo(userId);
		assertThat(profile.getCommunityId()).isEqualTo(communityId);
		assertThat(profile.getProfileImage()).isEqualTo(profileImage);
		assertThat(profile.getPersonalInfo()).isEqualTo(personalInfo);
		assertThat(profile.getPersonalInfo().getNickname()).isEqualTo(nickname);
		assertThat(profile.getPersonalInfo().getGender()).isEqualTo(gender);
		assertThat(profile.getPersonalInfo().getAgeRange()).isEqualTo(ageRange);
		assertThat(profile.getPersonalInfo().getResidence()).isEqualTo(residence);
		assertThat(profile.getPersonalInfo().getHometown()).isEqualTo(hometown);
	}

	@Test
	@DisplayName("프로필을 업데이트할 수 있다")
	void updateProfile() {
		// given
		Long userId = 1L;
		Long communityId = 1L;
		String profileImage = "profile-image-url";

		String nickname = "토론왕";
		GenderType gender = GenderType.MALE;
		AgeRangeType ageRange = AgeRangeType.TWENTIES;
		Region residence = Region.of(ProvinceType.SEOUL, DistrictType.GANGNAM);
		Region hometown = Region.of(ProvinceType.BUSAN, DistrictType.HAEUNDAE);
		PersonalInfo personalInfo = PersonalInfo.of(nickname, gender, ageRange, residence, hometown);

		Profile profile = Profile.create(userId, communityId, profileImage, personalInfo);

		Long newCommunityId = 2L;
		String newProfileImage = "new-profile-image-url";

		String newNickname = "토론마스터";
		GenderType newGender = GenderType.FEMALE;
		AgeRangeType newAgeRange = AgeRangeType.THIRTIES;
		Region newResidence = Region.of(ProvinceType.INCHEON, DistrictType.NAMDONG);
		Region newHometown = Region.of(ProvinceType.DAEGU, DistrictType.SUSEONG);
		PersonalInfo newPersonalInfo = PersonalInfo.of(newNickname, newGender, newAgeRange, newResidence, newHometown);

		// when
		profile.update(newCommunityId, newProfileImage, newPersonalInfo);

		// then
		assertThat(profile.getCommunityId()).isEqualTo(newCommunityId);
		assertThat(profile.getProfileImage()).isEqualTo(newProfileImage);
		assertThat(profile.getPersonalInfo()).isEqualTo(newPersonalInfo);
		assertThat(profile.getPersonalInfo().getNickname()).isEqualTo(newNickname);
		assertThat(profile.getPersonalInfo().getGender()).isEqualTo(newGender);
		assertThat(profile.getPersonalInfo().getAgeRange()).isEqualTo(newAgeRange);
		assertThat(profile.getPersonalInfo().getResidence()).isEqualTo(newResidence);
		assertThat(profile.getPersonalInfo().getHometown()).isEqualTo(newHometown);
	}

	@Test
	@DisplayName("커뮤니티 타입을 가져올 수 있다")
	void getCommunityType() {
		// given
		Long userId = 1L;
		Long communityId = 1L; // DC_INSIDE
		String profileImage = "profile-image-url";

		String nickname = "토론왕";
		GenderType gender = GenderType.MALE;
		AgeRangeType ageRange = AgeRangeType.TWENTIES;
		Region residence = Region.of(ProvinceType.SEOUL, DistrictType.GANGNAM);
		Region hometown = Region.of(ProvinceType.BUSAN, DistrictType.HAEUNDAE);
		PersonalInfo personalInfo = PersonalInfo.of(nickname, gender, ageRange, residence, hometown);

		Profile profile = Profile.create(userId, communityId, profileImage, personalInfo);

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
		Long userId = 1L;
		Long communityId = null;
		String profileImage = "profile-image-url";

		String nickname = "토론왕";
		GenderType gender = GenderType.MALE;
		AgeRangeType ageRange = AgeRangeType.TWENTIES;
		Region residence = Region.of(ProvinceType.SEOUL, DistrictType.GANGNAM);
		Region hometown = Region.of(ProvinceType.BUSAN, DistrictType.HAEUNDAE);
		PersonalInfo personalInfo = PersonalInfo.of(nickname, gender, ageRange, residence, hometown);

		Profile profile = Profile.builder()
			.userId(userId)
			.communityId(communityId)
			.profileImage(profileImage)
			.personalInfo(personalInfo)
			.build();

		// when
		CommunityType communityType = profile.getCommunityType();

		// then
		assertThat(communityType).isNull();
	}

	@Test
	@DisplayName("PersonalInfo의 정보를 올바르게 가져올 수 있다")
	void getPersonalInfoDetails() {
		// given
		Long userId = 1L;
		Long communityId = 1L;
		String profileImage = "profile-image-url";

		String nickname = "토론왕";
		GenderType gender = GenderType.MALE;
		AgeRangeType ageRange = AgeRangeType.TWENTIES;
		Region residence = Region.of(ProvinceType.SEOUL, DistrictType.GANGNAM);
		Region hometown = Region.of(ProvinceType.BUSAN, DistrictType.HAEUNDAE);
		PersonalInfo personalInfo = PersonalInfo.of(nickname, gender, ageRange, residence, hometown);

		Profile profile = Profile.create(userId, communityId, profileImage, personalInfo);

		// when & then
		assertThat(profile.getPersonalInfo().getNickname()).isEqualTo(nickname);
		assertThat(profile.getPersonalInfo().getGender()).isEqualTo(gender);
		assertThat(profile.getPersonalInfo().getAgeRange()).isEqualTo(ageRange);

		// 거주지 정보 확인
		assertThat(profile.getPersonalInfo().getResidence().getProvinceType()).isEqualTo(ProvinceType.SEOUL);
		assertThat(profile.getPersonalInfo().getResidence().getDistrictType()).isEqualTo(DistrictType.GANGNAM);

		// 출신지 정보 확인
		assertThat(profile.getPersonalInfo().getHometown().getProvinceType()).isEqualTo(ProvinceType.BUSAN);
		assertThat(profile.getPersonalInfo().getHometown().getDistrictType()).isEqualTo(DistrictType.HAEUNDAE);
	}
}