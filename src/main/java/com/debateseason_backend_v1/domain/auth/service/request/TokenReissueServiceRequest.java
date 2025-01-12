package com.debateseason_backend_v1.domain.auth.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenReissueServiceRequest {

	private String refreshToken;

	@Builder
	private TokenReissueServiceRequest(String refreshToken) {

		this.refreshToken = refreshToken;
	}

}
