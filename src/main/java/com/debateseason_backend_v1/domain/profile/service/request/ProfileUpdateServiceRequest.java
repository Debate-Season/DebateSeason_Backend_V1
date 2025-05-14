package com.debateseason_backend_v1.domain.profile.service.request;

import com.debateseason_backend_v1.domain.profile.domain.CommunityId;
import com.debateseason_backend_v1.domain.profile.domain.Nickname;
import com.debateseason_backend_v1.domain.profile.domain.PersonalInfo;
import com.debateseason_backend_v1.domain.profile.domain.ProfileUpdateCommand;
import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;
import com.debateseason_backend_v1.domain.user.domain.UserId;

import lombok.Builder;

@Builder
public record ProfileUpdateServiceRequest(
	UserId userId,
	CommunityId communityId,
	String profileColor,
	String nickname,
	GenderType gender,
	AgeRangeType ageRange
) {

	public ProfileUpdateCommand toCommand() {
		return new ProfileUpdateCommand(
			new PersonalInfo(profileColor, new Nickname(nickname), gender, ageRange),
			communityId
		);
	}
}
