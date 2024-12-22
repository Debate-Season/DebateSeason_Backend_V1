package com.debateseason_backend_v1.domain.user.controller.request;

import com.debateseason_backend_v1.common.enums.SocialType;
import com.debateseason_backend_v1.domain.user.service.request.SocialLoginServiceRequest;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginRequest {

	@NotBlank
	String externalId;

	@NotBlank
	SocialType socialType;

	public SocialLoginServiceRequest toServiceRequest() {

		return SocialLoginServiceRequest.builder()
			.externalId(externalId)
			.socialType(socialType)
			.build();
	}

}