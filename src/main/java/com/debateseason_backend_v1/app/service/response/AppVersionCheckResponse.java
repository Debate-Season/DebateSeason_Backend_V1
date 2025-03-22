package com.debateseason_backend_v1.app.service.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "앱 버전 체크 응답 DTO", description = "앱 버전 체크 응답")
public record AppVersionCheckResponse(
	@Schema(description = "강제 업데이트 필요 여부", example = "true")
	boolean forceUpdate,

	@Schema(description = "버전 코드", example = "25")
	Integer versionCode

) {

	public static AppVersionCheckResponse of(boolean forceUpdate, Integer versionCode) {
		return AppVersionCheckResponse.builder()
			.forceUpdate(forceUpdate)
			.versionCode(versionCode)
			.build();
	}
}
