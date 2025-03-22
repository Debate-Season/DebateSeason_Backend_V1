package com.debateseason_backend_v1.app.controller.docs;

import org.springframework.web.bind.annotation.RequestParam;

import com.debateseason_backend_v1.app.service.response.AppVersionCheckResponse;
import com.debateseason_backend_v1.common.response.ApiResult;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "App API", description = "앱 관련 API")
public interface AppVersionControllerV1Docs {

	@Parameter(
		name = "versionCode",
		description = "체크할 앱 버전 코드",
		required = true,
		example = "10"
	)
	public ApiResult<AppVersionCheckResponse> checkUpdate(
		@RequestParam("versionCode")
		Integer versionCode
	);
}
