package com.debateseason_backend_v1.domain.issue.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.issue.service.IssueServiceV1;
import com.debateseason_backend_v1.domain.user.service.UserIssueServiceV1;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserIssueControllerV1 {

	private final IssueServiceV1 issueServiceV1;
	private final UserIssueServiceV1 userIssueServiceV1;

	// 2. 이슈방 단건 불러오기(+ 채팅방도 같이 불러와야 함.)
	@Operation(
		summary = "이슈방 1건 상세보기",
		description = "이슈방 상세보기(+ 채팅방도 같이 불러와야 함.)")
	@GetMapping("/issue")
	public ApiResult<Object> getIssue(
		@RequestParam(name = "issue-id") Long issueId,
		@AuthenticationPrincipal CustomUserDetails principal,
		@RequestParam(name = "page")Integer page) {
		Long userId = principal.getUserId();
		//return issueServiceV1.fetch(issueId, userId);
		return issueServiceV1.fetch2(issueId, userId, page);
	}


	@Operation(
		summary = "이슈방 즐겨찾기 등록하기",
		description = "이슈방 1건을 즐겨찾기 등록합니다.")
	@GetMapping("/bookmark")
	public ApiResult<Object> bookMarkIssue(
		@RequestParam(name = "issue-id") Long issueId,
		@AuthenticationPrincipal CustomUserDetails principal) {

		Long userId = principal.getUserId();

		return issueServiceV1.booMark(issueId,userId);
	}


	// 2. 인덱스 페이지(홈)
	// 이슈방 전체 나열
	@Operation(
		summary = "이슈방 전체를 불러옵니다(수정가능)",
		description = " ")
	@GetMapping("/home")
	public ApiResult<Object> indexPage(
		@RequestParam(name = "page") Long page
	) {

		return issueServiceV1.fetchAll(page);
	}

	// 3. issueMap으로 이동하기
	// majorCategory와 middleCategory는 null일 수가 없을듯.

	@Operation(
		summary = "이슈맵 페이지로 이동합니다.",
		description = " ")
	@GetMapping("/issue-map")
	public ApiResult<Object> getIssueMap(
		@RequestParam(name = "page") Long page,
		@RequestParam(name = "majorcategory") String majorcategory) {


		return issueServiceV1.fetchIssueMap(page,majorcategory);
	}

}