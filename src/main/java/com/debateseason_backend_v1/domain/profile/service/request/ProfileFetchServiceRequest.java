package com.debateseason_backend_v1.domain.profile.service.request;

import com.debateseason_backend_v1.domain.user.domain.UserId;

import lombok.Builder;

@Builder
public record ProfileFetchServiceRequest(
	UserId userId
) {
}
