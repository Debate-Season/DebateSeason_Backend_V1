package com.debateseason_backend_v1.domain.user.controller.request;

import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.user.service.request.SocialLoginServiceRequest;

import jakarta.validation.constraints.NotNull;

public record SocialLoginRequest(
	@NotNull(message = "외부 ID는 필수입니다")
	String externalId,

	@NotNull(message = "외부 ID는 필수입니다")
	SocialType socialType,

	String idToken
) {

	public SocialLoginServiceRequest toServiceRequest() {
		return SocialLoginServiceRequest.builder()
			.externalId(externalId)
			.socialType(socialType)
			.build();
	}

}