package com.debateseason_backend_v1.domain.profile.application.service.request;

import lombok.Builder;

@Builder
public record ProfileImageRegisterServiceRequest(
	Long userId,
	String profileImage
) {
}
