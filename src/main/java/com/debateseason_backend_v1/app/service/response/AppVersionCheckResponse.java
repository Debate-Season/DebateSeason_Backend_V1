package com.debateseason_backend_v1.app.service.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "앱 버전 체크 응답 DTO", description = "앱 버전 체크 응답")
public record AppVersionCheckResponse(
	@Schema(description = "강제 업데이트 필요 여부", example = "true")
	boolean forceUpdate
) {

	public static AppVersionCheckResponse of(boolean forceUpdate) {
		return new AppVersionCheckResponse(forceUpdate);
	}
}
