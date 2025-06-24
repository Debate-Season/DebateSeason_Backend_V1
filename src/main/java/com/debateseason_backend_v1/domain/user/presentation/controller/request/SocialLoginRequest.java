package com.debateseason_backend_v1.domain.user.presentation.controller.request;

import com.debateseason_backend_v1.domain.user.application.service.request.SocialLoginServiceRequest;
import com.debateseason_backend_v1.domain.user.domain.SocialType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(title = "소셜 로그인 DTO", description = "소셜 로그인 요청")
public record SocialLoginRequest(

	@Schema(description = "사용자 고유 식별자", example = "1323412")
	@NotBlank(message = "소셜 고유 ID는 필수입니다.")
	String identifier,

	@Schema(description = "로그인 요청 소셜 타입", example = "kakao")
	@NotNull(message = "소셜 타입은 필수입니다.")
	SocialType socialType
) {

	public SocialLoginServiceRequest toServiceRequest() {
		return SocialLoginServiceRequest.builder()
			.identifier(identifier)
			.socialType(socialType)
			.build();
	}

}