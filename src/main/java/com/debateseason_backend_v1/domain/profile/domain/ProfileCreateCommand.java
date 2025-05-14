package com.debateseason_backend_v1.domain.profile.domain;

import com.debateseason_backend_v1.domain.user.domain.UserId;

public record ProfileCreateCommand(
	PersonalInfo personalInfo,
	UserId userId,
	CommunityId communityId
) {
}
