package com.debateseason_backend_v1.domain.profile.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Profile {
	private Long id;
	private Long userId;
	private Long communityId;
	private String profileImage;
	private Nickname nickname;
	private GenderType gender;
	private AgeRangeType ageRange;
	private Region residence;
	private Region hometown;

	private Profile() {
	}

	@Builder
	private Profile(
		Long id, Long userId, Long communityId,
		String profileImage, Nickname nickname,
		GenderType gender, AgeRangeType ageRange,
		Region residence, Region hometown
	) {
		this.id = id;
		this.userId = userId;
		this.communityId = communityId;
		this.profileImage = profileImage;
		this.nickname = nickname;
		this.gender = gender;
		this.ageRange = ageRange;
		this.residence = residence;
		this.hometown = hometown;
	}

	public static Profile create(
		Long userId, Long communityId, Nickname nickname, GenderType gender,
		AgeRangeType ageRange, Region residence, Region hometown) {

		return Profile.builder()
			.profileImage("RED")
			.userId(userId)
			.communityId(communityId)
			.nickname(nickname)
			.gender(gender)
			.ageRange(ageRange)
			.residence(residence)
			.hometown(hometown)
			.build();
	}

	public void update(
		Long communityId, Nickname nickname,
		GenderType gender, AgeRangeType ageRange,
		Region residence, Region hometown
	) {
		this.communityId = communityId;
		this.nickname = nickname;
		this.gender = gender;
		this.ageRange = ageRange;
		this.residence = residence;
		this.hometown = hometown;
	}

	public void anonymize(String uuid) {
		this.nickname = Nickname.anonymize(uuid);
		this.gender = GenderType.UNDEFINED;
		this.ageRange = AgeRangeType.UNDEFINED;
		this.residence = Region.anonymize();
		this.hometown = Region.anonymize();
	}

	public void updateProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public CommunityType getCommunityType() {
		return CommunityType.findById(communityId);
	}

}
