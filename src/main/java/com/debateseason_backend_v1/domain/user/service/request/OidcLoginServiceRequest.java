package com.debateseason_backend_v1.domain.user.service.request;

import com.debateseason_backend_v1.domain.user.domain.SocialType;
import com.debateseason_backend_v1.domain.user.domain.UserRegisterCommand;

import lombok.Builder;

@Builder
public record OidcLoginServiceRequest(
	SocialType socialType,
	String idToken
) {

	public UserRegisterCommand toCommand() {
		return new UserRegisterCommand(idToken, socialType);
	}
}
