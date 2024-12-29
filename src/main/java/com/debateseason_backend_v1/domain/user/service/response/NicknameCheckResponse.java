package com.debateseason_backend_v1.domain.user.service.response;

import lombok.Builder;

@Builder
public record NicknameCheckResponse(
	boolean available
) {
}