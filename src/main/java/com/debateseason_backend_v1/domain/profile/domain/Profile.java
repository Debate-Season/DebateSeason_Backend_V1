package com.debateseason_backend_v1.domain.profile.domain;

import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;
import com.debateseason_backend_v1.domain.user.domain.UserId;

import lombok.Getter;

@Getter
public class Profile {

	public static final Profile EMPTY = new Profile(null, null, null, null);

	private ProfileId id;
	private PersonalInfo personalInfo;
	private UserId userId;
	private CommunityId communityId;

	public Profile(ProfileId id, UserId userId, CommunityId communityId, PersonalInfo personalInfo) {
		this.id = id;
		this.personalInfo = personalInfo;
		this.userId = userId;
		this.communityId = communityId;
	}

	public static Profile create(ProfileCreateCommand profileCreateCommand) {
		if (profileCreateCommand == null) {
			throw new IllegalArgumentException("profileCreateCommand must not be null");
		}

		return new Profile(
			new ProfileId(null),
			profileCreateCommand.userId(),
			profileCreateCommand.communityId(),
			profileCreateCommand.personalInfo()
		);
	}

	public Profile update(ProfileUpdateCommand profileUpdateCommand) {
		this.personalInfo = profileUpdateCommand.personalInfo();
		this.communityId = profileUpdateCommand.communityId();
		return this;
	}

	public Profile anonymize(String uuid) {
		this.personalInfo = new PersonalInfo(
			null, new Nickname(uuid), GenderType.UNDEFINED, AgeRangeType.UNDEFINED
		);
		return this;
	}

}
