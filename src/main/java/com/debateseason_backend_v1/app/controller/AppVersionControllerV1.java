package com.debateseason_backend_v1.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.app.controller.docs.AppVersionControllerV1Docs;
import com.debateseason_backend_v1.app.service.AppVersionServiceV1;
import com.debateseason_backend_v1.app.service.response.AppVersionCheckResponse;
import com.debateseason_backend_v1.common.response.ApiResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app")
public class AppVersionControllerV1 implements AppVersionControllerV1Docs {

	private final AppVersionServiceV1 appVersionServiceV1;

	@GetMapping("/versions/check")
	public ApiResult<AppVersionCheckResponse> checkUpdate(
		@RequestParam("versionCode") Integer versionCode
	) {

		AppVersionCheckResponse response = appVersionServiceV1.updateCheck(versionCode);

		return ApiResult.success("버전 체크에 성공했습니다.", response);
	}

}
