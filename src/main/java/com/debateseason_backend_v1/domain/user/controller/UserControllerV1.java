package com.debateseason_backend_v1.domain.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.issue.service.IssueServiceV1;
import com.debateseason_backend_v1.domain.user.controller.request.SocialLoginRequest;
import com.debateseason_backend_v1.domain.user.service.UserServiceV1;
import com.debateseason_backend_v1.domain.user.service.response.LoginResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {

	private final UserServiceV1 userServiceV1;
	private final IssueServiceV1 issueServiceV1;

	@PostMapping("/login")
	public ApiResult<LoginResponse> socialLogin(@RequestBody SocialLoginRequest request) {

		LoginResponse response = userServiceV1.socialLogin(request.toServiceRequest());

		return ApiResult.success("소셜 로그인 성공", response);
	}

	// 2. 인덱스 페이지(홈)
	// 이슈방 전체 나열
	@Operation(
		summary = "이슈방 전체를 불러옵니다(수정가능)",
		description = " ")
	@GetMapping("")
	public ApiResult<Object> indexPage() {
		return issueServiceV1.fetchAll();
	}

}
