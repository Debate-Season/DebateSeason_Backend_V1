package com.debateseason_backend_v1.domain.article.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.article.model.response.ArticlePublicResponse;
import com.debateseason_backend_v1.domain.article.service.ArticlePublicService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/** 토론철 아티클·사건 타임라인 공개 조회 (익명, PUBLIC_URLS). 고객사 B2B 조회와 경로를 섞지 않는다. */
@Tag(name = "Article API", description = "토론철 아티클·이슈 사건 타임라인 (익명)")
@RequiredArgsConstructor
@RestController
public class ArticleController {

	private final ArticlePublicService articlePublicService;

	@Operation(summary = "아티클 단건", description = "게시된 토론철판 아티클만. 미게시·회수·고객사판·없는 id 는 404 NOT_FOUND_ARTICLE.")
	@GetMapping("/api/v1/article")
	public ApiResult<ArticlePublicResponse.Detail> article(@RequestParam(name = "article-id") Long articleId) {
		return ApiResult.success("아티클", articlePublicService.article(articleId));
	}

	@Operation(summary = "이슈 사건 타임라인", description = "이슈에 연결된 사건, 발생일 내림차순(날짜 없는 사건은 뒤). 병합된 사건은 빠지고 "
		+ "그 아티클은 흡수한 사건 밑에 보입니다. 사건마다 게시 아티클 카드(최신 발행순). 보관(ARCHIVED) 이슈는 404.")
	@GetMapping("/api/v1/issue/events")
	public ApiResult<ArticlePublicResponse.Timeline> timeline(
		@RequestParam(name = "issue-id") Long issueId,
		@Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
		@Parameter(description = "페이지 크기 (기본 20, 최대 100)") @RequestParam(required = false) Integer size) {

		return ApiResult.success("이슈 사건 타임라인", articlePublicService.timeline(issueId, cursor, size));
	}
}
