package com.debateseason_backend_v1.domain.repository.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.domain.profile.domain.Region;
import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.profile.enums.DistrictType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;
import com.debateseason_backend_v1.domain.profile.enums.ProvinceType;

class ProfileTest {

	@Test
	@DisplayName("프로필을 생성할 수 있다")
	void createProfile() {
		// given
		Long userId = 1L;
		String profileColor = "RED";
		String nickname = "토론왕";
		Long communityId = 1L;
		GenderType gender = GenderType.MALE;
		AgeRangeType ageRange = AgeRangeType.TWENTIES;

		// when
		Profile profile = Profile.builder()
			.userId(userId)
			.profileImage(profileColor)
			.nickname(nickname)
			.communityId(communityId)
			.gender(gender)
			.ageRange(ageRange)
			.build();

		// then
		assertThat(profile.getUserId()).isEqualTo(userId);
		assertThat(profile.getProfileImage()).isEqualTo(profileColor);
		assertThat(profile.getNickname()).isEqualTo(nickname);
		assertThat(profile.getCommunityId()).isEqualTo(communityId);
		assertThat(profile.getGender()).isEqualTo(gender);
		assertThat(profile.getAgeRange()).isEqualTo(ageRange);
	}

	@Test
	@DisplayName("프로필을 업데이트할 수 있다")
	void updateProfile() {
		// given
		Profile profile = Profile.builder()
			.userId(1L)
			.profileImage("RED")
			.nickname("토론왕")
			.communityId(1L)
			.gender(GenderType.MALE)
			.ageRange(AgeRangeType.TWENTIES)
			.hometown(Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN))
			.residence(Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN))
			.build();

		String newProfileImage = "BLUE";
		String newNickname = "토론마스터";
		Long newCommunityId = 2L;
		GenderType newGender = GenderType.FEMALE;
		AgeRangeType newAgeRange = AgeRangeType.THIRTIES;
		Region newHometown = Region.of(ProvinceType.BUSAN, DistrictType.JUNG_BUSAN);
		Region newResidence = Region.of(ProvinceType.BUSAN, DistrictType.JUNG_BUSAN);

		// when
		profile.update(newProfileImage, newNickname, newCommunityId, newGender, newAgeRange, newHometown, newResidence);

		// then
		assertThat(profile.getProfileImage()).isEqualTo(newProfileImage);
		assertThat(profile.getNickname()).isEqualTo(newNickname);
		assertThat(profile.getCommunityId()).isEqualTo(newCommunityId);
		assertThat(profile.getGender()).isEqualTo(newGender);
		assertThat(profile.getAgeRange()).isEqualTo(newAgeRange);
	}

	@Test
	@DisplayName("프로필을 익명화할 수 있다")
	void anonymizeProfile() {
		// given
		Profile profile = Profile.builder()
			.userId(1L)
			.profileImage("RED")
			.nickname("토론왕")
			.communityId(1L)
			.gender(GenderType.MALE)
			.ageRange(AgeRangeType.TWENTIES)
			.hometown(Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN))
			.residence(Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN))
			.build();

		String anonymousNickname = "탈퇴한사용자";

		// when
		profile.anonymize(anonymousNickname);

		// then
		assertThat(profile.getNickname()).isEqualTo(anonymousNickname);
		assertThat(profile.getGender()).isEqualTo(GenderType.UNDEFINED);
	}

	@Test
	@DisplayName("커뮤니티 타입을 가져올 수 있다")
	void getCommunityType() {
		// given
		Long communityId = 1L; // DC_INSIDE
		Profile profile = Profile.builder()
			.userId(1L)
			.profileImage("RED")
			.nickname("토론왕")
			.communityId(communityId)
			.gender(GenderType.MALE)
			.ageRange(AgeRangeType.TWENTIES)
			.hometown(Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN))
			.residence(Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN))
			.build();

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
		Profile profile = Profile.builder()
			.userId(1L)
			.profileImage("RED")
			.nickname("토론왕")
			.communityId(null)
			.gender(GenderType.MALE)
			.ageRange(AgeRangeType.TWENTIES)
			.hometown(Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN))
			.residence(Region.of(ProvinceType.SEOUL, DistrictType.YONGSAN))
			.build();

		// when
		CommunityType communityType = profile.getCommunityType();

		// then
		assertThat(communityType).isNull();
	}
}