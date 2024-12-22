package com.debateseason_backend_v1.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.issue.service.IssueServiceV1;
import com.debateseason_backend_v1.domain.user.controller.request.SocialLoginRequest;
import com.debateseason_backend_v1.domain.user.service.UserServiceV1;
import com.debateseason_backend_v1.domain.user.service.response.AuthResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {

	private final UserServiceV1 userServiceV1;
	private final IssueServiceV1 issueServiceV1;

	@PostMapping("/login")
	public ApiResult<?> socialLogin(@RequestBody SocialLoginRequest request) {

		AuthResponse authResponse = userServiceV1.processSocialLogin(request);

		return ApiResult.success("소셜 로그인 성공", authResponse);
	}

	// 2. 인덱스 페이지(홈)
	// 이슈방 전체 나열
	@GetMapping("")
	public ResponseEntity<?> indexPage() {
		return issueServiceV1.fetchAll();
	}

}
