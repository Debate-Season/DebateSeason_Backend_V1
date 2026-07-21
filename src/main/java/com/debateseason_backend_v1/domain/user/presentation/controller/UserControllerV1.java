package com.debateseason_backend_v1.domain.user.presentation.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.common.response.VoidApiResult;
import com.debateseason_backend_v1.domain.issue.mapper.IssueBriefResponse;
import com.debateseason_backend_v1.domain.issue.application.service.IssueServiceV1;
import com.debateseason_backend_v1.domain.user.presentation.controller.docs.UserControllerV1Docs;
import com.debateseason_backend_v1.domain.user.presentation.controller.request.LogoutRequest;
import com.debateseason_backend_v1.domain.user.application.service.UserServiceV1;
import com.debateseason_backend_v1.domain.user.application.service.response.LoginResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 implements UserControllerV1Docs {

	private final UserServiceV1 userServiceV1;
	private final IssueServiceV1 issueServiceV1;

	/**
	 * 제거됨 — 이 엔드포인트는 인증 우회 경로였다.
	 *
	 * identifier(카카오/애플 소셜 ID) 문자열을 받아 아무 검증 없이 토큰을 발급했다.
	 * 소셜 ID 는 신원을 "식별"하는 값이지 본인임을 "증명"하는 값이 아니므로,
	 * 남의 ID 를 아는 사람이 그 계정으로 로그인할 수 있었고(탈퇴까지 가능),
	 * 임의 문자열로 계정을 무제한 생성할 수도 있었다.
	 *
	 * 정상 경로는 V2(`POST /api/v2/users/login`)다. id_token 을 검증해
	 * identifier 를 서버가 직접 추출하므로 위조할 수 없다.
	 * 앱·웹 모두 이미 V2 만 사용한다.
	 *
	 * 404 가 아니라 410 Gone 을 주는 이유: 로그로 사용 여부를 완전히 증명할 수 없어
	 * 혹시 남아 있을 구버전 클라이언트에게 "업데이트하라"는 신호를 주기 위함이다.
	 * 호출이 오면 GlobalExceptionHandler 가 WARN 으로 남긴다(code=REMOVED_API).
	 */
	@PostMapping("/login")
	public ApiResult<LoginResponse> login() {

		throw new CustomException(ErrorCode.REMOVED_API);
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
		summary = "이슈방 전체를 불러옵니다(수정가능)  ",
		description = " ")
	@GetMapping("/home")
	public ApiResult<List<IssueBriefResponse>> indexPage(
		@AuthenticationPrincipal CustomUserDetails principal
	) {
		return issueServiceV1.fetchV1();
	}

}
