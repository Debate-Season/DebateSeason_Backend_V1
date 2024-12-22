package com.debateseason_backend_v1.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.auth.dto.SocialLoginRequest;
import com.debateseason_backend_v1.domain.auth.service.AuthServiceV1;
import com.debateseason_backend_v1.domain.auth.service.response.AuthResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthControllerV1 {

	private final AuthServiceV1 authService;

	@PostMapping("/social/login")
	public ApiResult<?> socialLogin(@RequestBody SocialLoginRequest request) {
		log.info("Received request: {}", request);
		log.info("Social type value: {}", request.socialType());
		log.info("Social type name: {}", request.socialType());
		AuthResponse authResponse = authService.processSocialLogin(request);

		return ApiResult.success("소셜 로그인 성공", authResponse);
	}
}
