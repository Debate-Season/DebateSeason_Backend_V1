package com.debateseason_backend_v1.domain.issue.presentation.controller;

import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.ResponseOnlyHome;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomServiceV1;
import com.debateseason_backend_v1.domain.issue.model.response.PaginationDTO;
import com.debateseason_backend_v1.domain.issue.docs.IssueControllerV1Docs;
import com.debateseason_backend_v1.domain.issue.model.Category;
import com.debateseason_backend_v1.domain.issue.mapper.IssueDetailResponse;
import com.debateseason_backend_v1.domain.issue.application.service.IssueServiceV1;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class IssueControllerV1 implements IssueControllerV1Docs {

	private final IssueServiceV1 issueServiceV1;
	private final ChatRoomServiceV1 chatRoomServiceV1;

	@Override
	@GetMapping("/home/refresh")
	public ApiResult<ResponseOnlyHome> indexPage(
		@RequestParam(name = "page", required = false) Long page,
		@AuthenticationPrincipal CustomUserDetails principal
	) {
		Long userId = principal.getUserId();
		return chatRoomServiceV1.findVotedChatRoom(userId,page);

	}


	// 2. 이슈방 단건 불러오기(+ 채팅방도 같이 불러와야 함.)
	// issueId는 required = true
	@GetMapping("/issue")
	public ApiResult<IssueDetailResponse> getIssue(
		@RequestParam(name = "issue-id") Long issueId,
		@AuthenticationPrincipal CustomUserDetails principal,
		@RequestParam(name = "page",required = false)Long page) {
		Long userId = principal.getUserId();
		return issueServiceV1.fetchV2(issueId, userId, page);
	}


	@Operation(
		summary = "이슈방 즐겨찾기 등록하기",
		description = "이슈방 1건을 즐겨찾기 등록합니다.")
	@PostMapping("/bookmark")
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
	public ApiResult<PaginationDTO> getIssueMap(
		@RequestParam(name = "page",required = false) Long page,
		@RequestParam(name = "majorcategory",required = false) @Nullable Category majorcategory) {

		String Stringcategory = ( majorcategory == null ? null : majorcategory.toString() );

		return issueServiceV1.fetchIssueMap(page,Stringcategory); // 수정
	}

	// 4. 추천 게시글 가져오기
	@GetMapping("/home/recommend")
	public ApiResult<ResponseOnlyHome> getRecommend(
		@RequestParam(name = "page", required = false) Long page,
		@AuthenticationPrincipal CustomUserDetails principal
	) {
		Long userId = principal.getUserId();
		return chatRoomServiceV1.findVotedChatRoom(userId,page);

	}

}