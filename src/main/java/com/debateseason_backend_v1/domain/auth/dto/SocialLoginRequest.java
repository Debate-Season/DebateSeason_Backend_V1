package com.debateseason_backend_v1.domain.auth.dto;

import com.debateseason_backend_v1.common.enums.SocialType;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
	@NotBlank
	@JsonProperty("externalId")
	String externalId,

	@NotBlank  // @NotBlank 대신 @NotNull 사용
	@JsonProperty("socialType")
	SocialType socialType
) {
}