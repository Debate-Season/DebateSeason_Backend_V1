package com.debateseason_backend_v1.domain.user.service.request;

import com.debateseason_backend_v1.common.enums.SocialType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginServiceRequest {

	String externalId;
	SocialType socialType;

	@Builder
	private SocialLoginServiceRequest(String externalId, SocialType socialType) {
		
		this.externalId = externalId;
		this.socialType = socialType;
	}

}
