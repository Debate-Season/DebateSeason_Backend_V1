package com.debateseason_backend_v1.domain.user.domain;

import lombok.Builder;

@Builder
public record UserMappingData(
	Long id,
	String identifier,
	SocialType socialType,
	UserStatus status
) {
}
