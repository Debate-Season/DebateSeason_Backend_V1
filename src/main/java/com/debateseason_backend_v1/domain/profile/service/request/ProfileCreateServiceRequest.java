package com.debateseason_backend_v1.domain.profile.service.request;

import com.debateseason_backend_v1.domain.profile.domain.CommunityId;
import com.debateseason_backend_v1.domain.profile.domain.PersonalInfo;
import com.debateseason_backend_v1.domain.profile.domain.ProfileCreateCommand;
import com.debateseason_backend_v1.domain.user.domain.UserId;

import lombok.Builder;

@Builder
public record ProfileCreateServiceRequest(
	PersonalInfo personalInfo,
	UserId userId,
	CommunityId communityId
) {

	public ProfileCreateCommand toCommand() {
		return new ProfileCreateCommand(
			personalInfo,
			userId,
			communityId
		);
	}
}
