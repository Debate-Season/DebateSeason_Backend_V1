package com.debateseason_backend_v1.domain.profile.domain;

import com.debateseason_backend_v1.domain.user.domain.UserId;

import lombok.Getter;

@Getter
public class Profile {

	public static final Profile EMPTY = new Profile(null, null, null, null);

	private ProfileId id;
	private UserId userId;
	private CommunityId communityId;
	private PersonalInfo personalInfo;

	public Profile(ProfileId id, UserId userId, CommunityId communityId, PersonalInfo personalInfo) {
		this.id = id;
		this.personalInfo = personalInfo;
		this.userId = userId;
		this.communityId = communityId;
	}

	public static Profile create(ProfileCreateCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("profileCreateCommand must not be null");
		}

		return new Profile(
			ProfileId.EMPTY,
			command.userId(),
			command.communityId(),
			command.personalInfo()
		);
	}

	public Profile update(ProfileUpdateCommand command) {
		this.personalInfo = command.personalInfo();
		this.communityId = command.communityId();
		return this;
	}

	public Profile anonymize(String uuid) {
		this.personalInfo = new PersonalInfo(
			null, new Nickname(uuid), GenderType.UNDEFINED, AgeRangeType.UNDEFINED
		);
		return this;
	}

}
