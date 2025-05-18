package com.debateseason_backend_v1.domain.profile.domain;

public record ProfileUpdateCommand(
	PersonalInfo personalInfo,
	CommunityId communityId
) {
}
