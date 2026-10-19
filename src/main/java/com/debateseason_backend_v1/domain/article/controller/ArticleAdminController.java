package com.debateseason_backend_v1.domain.article.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.article.model.request.ArticleEditRequest;
import com.debateseason_backend_v1.domain.article.model.request.ArticleLinksRequest;
import com.debateseason_backend_v1.domain.article.model.request.ArticleStatusChangeRequest;
import com.debateseason_backend_v1.domain.article.model.request.ArticleUpsertRequest;
import com.debateseason_backend_v1.domain.article.model.response.ArticleAdminResponse;
import com.debateseason_backend_v1.domain.article.service.ArticleAdminService;
import com.debateseason_backend_v1.security.service.Actor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** 토론철 아티클 적재·상태·연결. ADMIN JWT 또는 서비스 키. 데이터 레포가 데스킹을 마친 기사를 보낸다. */
@Tag(name = "Article Admin API", description = "토론철 아티클 적재·발행 — ADMIN 또는 서비스 키")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/article")
public class ArticleAdminController {

	private final ArticleAdminService articleAdminService;

	@Operation(summary = "아티클 적재", description = "새 articleKey 면 REVIEW 로 만들고, 있으면 한 번도 게시되지 않았을 때만 내용을 갱신합니다. "
		+ "게시된 적이 있으면 409. sourceMediaIds·issueIds·newsEventIds 중 없는 id 가 있으면 404 + 무저장.")
	@PostMapping
	public ApiResult<ArticleAdminResponse.Upsert> upsert(@Valid @RequestBody ArticleUpsertRequest request,
		Authentication authentication) {

		return ApiResult.success("아티클을 적재했습니다.", articleAdminService.upsert(request, Actor.of(authentication)));
	}

	@Operation(summary = "아티클 상태 변경", description = "REVIEW→PUBLISHED·HIDDEN, PUBLISHED↔HIDDEN. 같은 상태면 changed=false. "
		+ "처음 PUBLISHED 가 될 때만 datePublished 를 찍습니다.")
	@PatchMapping("/{articleId}/status")
	public ApiResult<ArticleAdminResponse.StatusChange> changeStatus(@PathVariable Long articleId,
		@Valid @RequestBody ArticleStatusChangeRequest request, Authentication authentication) {

		return ApiResult.success("아티클 상태를 반영했습니다.",
			articleAdminService.changeStatus(articleId, request, Actor.of(authentication)));
	}

	@Operation(summary = "아티클 연결 교체", description = "보낸 목록(newsEventIds·issueIds)만 통째로 바꿉니다. null 인 목록은 그대로.")
	@PutMapping("/{articleId}/links")
	public ApiResult<ArticleAdminResponse.Links> replaceLinks(@PathVariable Long articleId,
		@Valid @RequestBody ArticleLinksRequest request, Authentication authentication) {

		return ApiResult.success("아티클 연결을 바꿨습니다.",
			articleAdminService.replaceLinks(articleId, request, Actor.of(authentication)));
	}

	@Operation(summary = "아티클 확인 (articleKey)", description = "적재 확인용.")
	@GetMapping
	public ApiResult<ArticleAdminResponse.Summary> findByKey(@RequestParam String articleKey) {
		return ApiResult.success("아티클", articleAdminService.findByKey(articleKey));
	}

	@Operation(summary = "아티클 수정", description = "보낸 필드(headline·summary·body·keywords·articleSection)만 바꿉니다. 게시된 기사도 됩니다. "
		+ "수정 전 내용은 리비전으로 남고, 응답의 changeLogId 로 POST /admin/change-log/{id}/revert 하면 되돌립니다. "
		+ "keywords 를 보내면 키워드 색인도 갱신합니다. 고객사 응답 캐시는 바로 비웁니다. proposalId 로 멱등.")
	@PatchMapping("/{articleId}")
	public ApiResult<ArticleAdminResponse.Edit> edit(@PathVariable Long articleId,
		@Valid @RequestBody ArticleEditRequest request, Authentication authentication) {

		return ApiResult.success("아티클을 수정했습니다.", articleAdminService.edit(articleId, request, Actor.of(authentication)));
	}
}
