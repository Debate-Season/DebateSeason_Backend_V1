package com.debateseason_backend_v1.domain.auth.service.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenReissueResponse {

	private String accessToken;
	private String refreshToken;

	@Builder
	private TokenReissueResponse(String accessToken, String refreshToken) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}
	
}
