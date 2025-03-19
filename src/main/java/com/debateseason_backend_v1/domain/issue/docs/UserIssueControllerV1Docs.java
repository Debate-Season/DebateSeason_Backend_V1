package com.debateseason_backend_v1.domain.issue.docs;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.debateseason_backend_v1.common.response.ApiResult;

import com.debateseason_backend_v1.domain.chatroom.model.response.RealHomeResponse;
import com.debateseason_backend_v1.domain.issue.model.response.IssueDetailResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Issue API", description = "이슈방 관련된 모든 API")
public interface UserIssueControllerV1Docs {
	@Operation(
		summary = "이슈방 1건 상세보기",
		description = "1개의 이슈방에 대한 관련된 모든 채팅방들을 가져오기",
		security = @SecurityRequirement(name = "JWT")
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "해당 이슈방을 성공적으로 조회했습니다."),
		@ApiResponse(responseCode = "400", description = "해당 이슈방은 존재하지 않습니다")
	})

	@Parameter(
		name = "issue-id",
		description = "이슈방 id",
		required = true,
		example = "1"
	)
	public ApiResult<IssueDetailResponse> getIssue(
		@RequestParam(name = "issue-id") Long issueId,
		@AuthenticationPrincipal CustomUserDetails principal,
		@RequestParam(name = "page",required = false)Long page
	);


	@Operation(
		summary = "Renewal된 Home ",
		description = "배포에 해당하는 Home",
		security = @SecurityRequirement(name = "JWT")
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "해당 이슈방을 성공적으로 조회했습니다."),
		@ApiResponse(responseCode = "400", description = "해당 이슈방은 존재하지 않습니다")
	})

	@Parameter(
		name = "page",
		description = "커서 페이지네이션 넘버. 입력 안해도 상관없음",
		required = false,
		example = "1"
	)
	public ApiResult<RealHomeResponse> indexPage(
		@RequestParam(name = "page", required = false) Long page,
		@AuthenticationPrincipal CustomUserDetails principal
	);

	//
	@Operation(
		summary = "이슈방 1건 즐겨찾기",
		description = "1건의 이슈방에 대해서 즐겨찾기",
		security = @SecurityRequirement(name = "JWT")
	)
	@Parameter(
		name = "issue-id",
		description = "이슈방 id",
		required = true,
		example = "1",
		schema = @Schema(type = "string")
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "해당 이슈방을 성공적으로 즐겨찾기 했습니다."),
		@ApiResponse(responseCode = "400", description = "해당 이슈방 즐겨찾기 실패했습니다.")
	})
	public ApiResult<String> bookMarkIssue(
		@RequestParam(name = "issue-id") Long issueId,
		@AuthenticationPrincipal CustomUserDetails principal);
	//

	/*
	@Operation(
		summary = "이슈맵 조회하기",
		description = "이슈맵을 호출합니다.",
		security = @SecurityRequirement(name = "JWT")
	)
	@Parameter(
		name = "page",
		description = "커서를 위한 값인 null일 수 있다.",
		required = true,
		example = "1",
		schema = @Schema(type = "string")
	)
	@Parameter(
		name = "majorcategory",
		description = "대분류를 의미하며, null일 수 있다.",
		required = true,
		example = "1",
		schema = @Schema(type = "string")
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "성공적으로 issue를 불러왔습니다."),
		@ApiResponse(responseCode = "400", description = "majorcategory를 잘못입력했습니다.")
	})
	@CustomApiErrorCode(
		{
			CustomErrorCode.NOT_FOUND_ISSUE
		}
	)
	public ApiResult<List<IssueResponse>> getIssueMap(
		@RequestParam(name = "page",required = false) Long page,
		@RequestParam(name = "majorcategory",required = false) String majorcategory)

		;

	 */
}
