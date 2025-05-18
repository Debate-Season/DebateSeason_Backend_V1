package com.debateseason_backend_v1.domain.profile.controller.request;

import com.debateseason_backend_v1.domain.profile.service.request.ProfileFetchServiceRequest;
import com.debateseason_backend_v1.domain.user.domain.UserId;

public record ProfileFetchRequest(
) {

	public ProfileFetchServiceRequest toServiceRequest(Long userId) {
		return new ProfileFetchServiceRequest(
			new UserId(userId)
		);
	}
}
