package com.debateseason_backend_v1.domain.user.controller.request;

import java.util.Arrays;

import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.user.service.request.SocialLoginServiceRequest;

import jakarta.validation.constraints.NotNull;

public record SocialLoginRequest(
	@NotNull(message = "외부 ID는 필수입니다")
	String externalId,

	@NotNull(message = "외부 ID는 필수입니다")
	String socialType,

	String idToken
) {

	public SocialLoginServiceRequest toServiceRequest() {
		return new SocialLoginServiceRequest(
			externalId,
			convertToSocialType(socialType)
		);
	}

	private SocialType convertToSocialType(String socialType) {
		return Arrays.stream(SocialType.values())
			.filter(type -> type.getDescription().equals(socialType))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("지원하지 않는 소셜 로그인 타입입니다: " + socialType));
	}

}