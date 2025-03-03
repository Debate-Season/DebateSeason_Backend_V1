package com.debateseason_backend_v1.domain.issue.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.domain.issue.docs.UserIssueControllerV1Docs;
import com.debateseason_backend_v1.domain.issue.dto.IssueDAO;
import com.debateseason_backend_v1.domain.issue.model.response.IssueResponse;
import com.debateseason_backend_v1.domain.issue.service.IssueServiceV1;
import com.debateseason_backend_v1.domain.user.service.UserIssueServiceV1;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserIssueControllerV1 implements UserIssueControllerV1Docs {

	private final IssueServiceV1 issueServiceV1;

	// 2. 이슈방 단건 불러오기(+ 채팅방도 같이 불러와야 함.)
	@GetMapping("/issue")
	public ApiResult<IssueDAO> getIssue(
		@RequestParam(name = "issue-id") Long issueId,
		@AuthenticationPrincipal CustomUserDetails principal,
		@RequestParam(name = "page",required = false)Long page) {
		Long userId = principal.getUserId();
		return issueServiceV1.fetch2(issueId, userId, page);
	}


	@Operation(
		summary = "이슈방 즐겨찾기 등록하기",
		description = "이슈방 1건을 즐겨찾기 등록합니다.")
	@GetMapping("/bookmark")
	public ApiResult<String> bookMarkIssue(
		@RequestParam(name = "issue-id") Long issueId,
		@AuthenticationPrincipal CustomUserDetails principal) {

		Long userId = principal.getUserId();

		return issueServiceV1.bookMark(issueId,userId);
	}

	// 3. issueMap으로 이동하기
	// majorCategory와 middleCategory는 null일 수가 없을듯.

	@Operation(
		summary = "이슈맵 페이지로 이동합니다.",
		description = " ")
	@GetMapping("/issue-map")
	public ApiResult<List<IssueResponse>> getIssueMap(
		@RequestParam(name = "page",required = false) Long page,
		@RequestParam(name = "majorcategory",required = false) String majorcategory) {


		return issueServiceV1.fetchIssueMap(page,majorcategory);
	}

}