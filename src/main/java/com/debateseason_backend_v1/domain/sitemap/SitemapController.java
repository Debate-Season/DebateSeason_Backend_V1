package com.debateseason_backend_v1.domain.sitemap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 웹 사이트맵용 피드. 익명 공개(PUBLIC_URLS). 웹이 빌드·재생성 때 1시간에 한 번 페이지를 넘기며 부른다.
 */
@Tag(name = "Sitemap API", description = "웹 사이트맵용 id + 수정 시각 피드 (익명)")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/sitemap")
public class SitemapController {

	private final SitemapService sitemapService;

	@Operation(summary = "이슈 사이트맵 피드",
		description = "노출 중인 이슈(PUBLISHED·DORMANT)만, issueId 오름차순. updatedAt 은 이슈 생성·수정, 노출 중인 토론 주제 추가, "
			+ "위키 게시 중 가장 늦은 시각. 다음 페이지는 nextCursor 로, null 이면 끝.")
	@GetMapping("/issues")
	public ApiResult<SitemapEntry.Page<SitemapEntry.Issue>> issues(
		@Parameter(description = "이전 응답의 nextCursor. 첫 페이지는 비움") @RequestParam(required = false) Long cursor,
		@Parameter(description = "페이지 크기 (기본 1000, 최대 5000)") @RequestParam(required = false) Integer size) {

		return ApiResult.success("이슈 사이트맵 피드", sitemapService.issues(cursor, size));
	}

	@Operation(summary = "토론 주제 사이트맵 피드",
		description = "노출 중인 토론 주제(스레드, OPEN·CLOSED, 소속 이슈 노출 중)만, threadId 오름차순. "
			+ "lastActivityAt 은 마지막 채팅 시각, 채팅이 없으면 생성 시각. 다음 페이지는 nextCursor 로, null 이면 끝.")
	@GetMapping("/threads")
	public ApiResult<SitemapEntry.Page<SitemapEntry.Thread>> threads(
		@Parameter(description = "이전 응답의 nextCursor. 첫 페이지는 비움") @RequestParam(required = false) Long cursor,
		@Parameter(description = "페이지 크기 (기본 1000, 최대 5000)") @RequestParam(required = false) Integer size) {

		return ApiResult.success("토론 주제 사이트맵 피드", sitemapService.threads(cursor, size));
	}
}
