package com.debateseason_backend_v1.domain.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.auth.controller.docs.AuthControllerV1Docs;
import com.debateseason_backend_v1.domain.auth.controller.request.TokenReissueRequest;
import com.debateseason_backend_v1.domain.auth.service.AuthServiceV1;
import com.debateseason_backend_v1.domain.auth.service.response.TokenReissueResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthControllerV1 implements AuthControllerV1Docs {

	private final AuthServiceV1 authService;

	@PostMapping("/reissue")
	public ApiResult<TokenReissueResponse> reissueToken(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody TokenReissueRequest request
	) {

		TokenReissueResponse tokenReissueResponse = authService.reissueToken(
			request.toServiceRequest(userDetails.getUserId())
		);

		return ApiResult.success("토큰 재발급에 성공했습니다.", tokenReissueResponse);
	}
}
