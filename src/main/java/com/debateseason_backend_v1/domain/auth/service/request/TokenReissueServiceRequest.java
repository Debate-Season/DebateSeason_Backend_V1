package com.debateseason_backend_v1.domain.auth.service.request;

import lombok.Builder;

@Builder
public record TokenReissueServiceRequest(
	String refreshToken
) {
}
