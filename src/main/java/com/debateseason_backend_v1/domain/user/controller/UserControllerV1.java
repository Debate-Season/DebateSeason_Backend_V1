package com.debateseason_backend_v1.domain.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.response.VoidApiResult;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.domain.issue.service.IssueServiceV1;
import com.debateseason_backend_v1.domain.user.controller.docs.UserControllerV1Docs;
import com.debateseason_backend_v1.domain.user.controller.request.LogoutRequest;
import com.debateseason_backend_v1.domain.user.controller.request.SocialLoginRequest;
import com.debateseason_backend_v1.domain.user.service.UserServiceV1;
import com.debateseason_backend_v1.domain.user.service.response.LoginResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 implements UserControllerV1Docs {

	private final UserServiceV1 userServiceV1;
	private final ChatRoomServiceV1 chatRoomServiceV1;

	@PostMapping("/login")
	public ApiResult<LoginResponse> login(@Valid @RequestBody SocialLoginRequest request) {

		LoginResponse response = userServiceV1.socialLogin(request.toServiceRequest());

		return ApiResult.success("로그인을 성공했습니다.", response);
	}

	@PostMapping("/logout")
	public VoidApiResult logout(
		@Valid @RequestBody LogoutRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {

		userServiceV1.logout(request.toServiceRequest(userDetails.getUserId()));

		return VoidApiResult.success("로그아웃을 성공했습니다.");
	}

	@PostMapping("/withdraw")
	public VoidApiResult withdraw(
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {

		userServiceV1.withdraw(userDetails.getUserId());

		return VoidApiResult.success("회원 탈퇴가 완료되었습니다.");
	}

	// 2. 인덱스 페이지(홈)
	// 이슈방 전체 나열
	@Operation(
		summary = "이슈방 전체를 불러옵니다(수정가능)",
		description = " ")
	@GetMapping("/home")
	public ApiResult<Object> indexPage(
		@RequestParam(name = "page") int page,
		@AuthenticationPrincipal CustomUserDetails principal
	) {
		Long userId = principal.getUserId();
		return chatRoomServiceV1.findVotedChatRoom(userId,page);
	}

}
