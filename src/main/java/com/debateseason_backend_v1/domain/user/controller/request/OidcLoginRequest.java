package com.debateseason_backend_v1.domain.user.controller.request;

import com.debateseason_backend_v1.domain.user.enums.SocialType;
import com.debateseason_backend_v1.domain.user.service.request.OidcLoginServiceRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(title = "OIDC 로그인 DTO", description = "OIDC 로그인 요청")
public record OidcLoginRequest(
	@Schema(description = "로그인 요청 소셜 타입", example = "kakao")
	@NotNull(message = "소셜 타입은 필수입니다.")
	SocialType socialType,

	@Schema(description = "ID Token", example = "eyJraWQiOiJkTWxFUkJhRmRLIiwiYWxnIjoiUlMyNTYifQ.eyJpc3M...")
	@NotBlank(message = "ID Token은 필수입니다.")
	String idToken
) {

	public OidcLoginServiceRequest toServiceRequest() {

		return OidcLoginServiceRequest.builder()
			.socialType(socialType)
			.idToken(idToken)
			.build();
	}
}
