package com.debateseason_backend_v1.domain.wiki.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.wiki.model.response.WikiResponse;
import com.debateseason_backend_v1.domain.wiki.service.WikiServiceV2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Wiki API V2", description = "v1.4.0 AI 토론위키")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2")
public class WikiControllerV2 {

	private final WikiServiceV2 wikiServiceV2;

	// 이슈의 게시된 토론위키 조회. 미게시/미존재는 본문 없이 200(에러 아님).
	// optional-auth: 비로그인도 조회 가능(공개 문서).
	@Operation(
		summary = "이슈의 토론위키(게시본) 조회",
		description = "이슈당 1장. 게시본이 없으면 status/content 가 null 로 내려갑니다(에러 아님)."
	)
	@Parameter(name = "issue-id", description = "이슈 id", required = true, example = "6")
	@GetMapping("/wiki")
	public ApiResult<WikiResponse> fetch(
		@RequestParam(name = "issue-id") Long issueId) {

		WikiResponse response = wikiServiceV2.getPublishedWiki(issueId);

		return ApiResult.success("토론위키를 불러왔습니다.", response);
	}
}
