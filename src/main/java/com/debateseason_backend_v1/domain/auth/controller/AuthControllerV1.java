package com.debateseason_backend_v1.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.auth.controller.request.TokenReissueRequest;
import com.debateseason_backend_v1.domain.auth.service.AuthServiceV1;
import com.debateseason_backend_v1.domain.auth.service.response.TokenReissueResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthControllerV1 {

	private final AuthServiceV1 authService;

	@PostMapping("/reissue")
	public ApiResult<?> reissueAccessToken(@RequestBody TokenReissueRequest request) {

		TokenReissueResponse tokenReissueResponse = authService.reissueToken(request.toServiceRequest());

		return ApiResult.success("토큰 재발급에 성공했습니다.", tokenReissueResponse);
	}
}
