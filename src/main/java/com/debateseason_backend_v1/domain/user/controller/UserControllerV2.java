package com.debateseason_backend_v1.domain.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.user.controller.docs.UserControllerV2Docs;
import com.debateseason_backend_v1.domain.user.controller.request.OidcLoginRequest;
import com.debateseason_backend_v1.domain.user.service.UserServiceV2;
import com.debateseason_backend_v1.domain.user.service.response.LoginResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/users")
public class UserControllerV2 implements UserControllerV2Docs {

	private final UserServiceV2 userServiceV2;

	@PostMapping("/login")
	public ApiResult<LoginResponse> login(@RequestBody OidcLoginRequest request) {

		LoginResponse response = userServiceV2.socialLogin(request.toServiceRequest());

		return ApiResult.success("로그인을 성공했습니다.", response);
	}

}
