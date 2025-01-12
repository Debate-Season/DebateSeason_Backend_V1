package com.debateseason_backend_v1.domain.auth.controller.request;

import com.debateseason_backend_v1.domain.auth.service.request.TokenReissueServiceRequest;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenReissueRequest {

	@NotBlank
	private String refreshToken;

	public TokenReissueServiceRequest toServiceRequest() {

		return TokenReissueServiceRequest.builder()
			.refreshToken(refreshToken)
			.build();
	}
}
