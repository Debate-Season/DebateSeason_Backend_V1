package com.debateseason_backend_v1.domain.wiki.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.wiki.model.request.WikiDraftUploadRequest;
import com.debateseason_backend_v1.domain.wiki.model.request.WikiManualRevisionRequest;
import com.debateseason_backend_v1.domain.wiki.model.request.WikiPublishRequest;
import com.debateseason_backend_v1.domain.wiki.model.response.WikiPublishResponse;
import com.debateseason_backend_v1.domain.wiki.model.response.WikiRevisionResponse;
import com.debateseason_backend_v1.domain.wiki.service.WikiGenerationService;
import com.debateseason_backend_v1.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * v1.4.0 — 토론위키 생성·검수 (ADMIN 전용, Phase 2).
 *
 * 경로가 {@code /api/v1/admin/**} 라 {@code WebSecurityConfig} 에서 이미 {@code hasRole("ADMIN")} 로 잠겨 있다.
 * 예외는 {@code /drafts} 하나 — 로컬 생성 스크립트가 서비스 키로 DRAFT 를 올린다. 게시({@code /publish})는 ADMIN 만.
 * 생성은 항상 DRAFT 로만 저장되고, ADMIN 이 확인 후 {@code /publish} 로 게시한다(자동 게시 금지).
 */
@Tag(name = "Wiki Admin API", description = "v1.4.0 AI 토론위키 — ADMIN 생성/게시/편집")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/wiki")
public class WikiAdminController {

	private final WikiGenerationService wikiGenerationService;

	@Operation(
		summary = "토론위키 초안(AI) 생성",
		description = "이슈의 원천 재료로 LLM 초안을 생성해 DRAFT 리비전으로 저장합니다(게시 안 함). "
			+ "생성기 미구성 시 503 을 반환합니다."
	)
	@PostMapping("/generate")
	public ApiResult<WikiRevisionResponse> generate(
		@RequestParam(name = "issue-id") Long issueId) {

		WikiRevisionResponse response = wikiGenerationService.generate(issueId);
		return ApiResult.success("토론위키 초안을 생성했습니다.", response);
	}

	@Operation(
		summary = "토론위키 리비전 게시",
		description = "지정한 리비전을 PUBLISHED 로 전환합니다. 옛 리비전 id 로 다시 게시하면 롤백입니다."
	)
	@PostMapping("/publish")
	public ApiResult<WikiPublishResponse> publish(
		@Valid @RequestBody WikiPublishRequest request) {

		WikiPublishResponse response = wikiGenerationService.publish(request.revisionId());
		return ApiResult.success("토론위키를 게시했습니다.", response);
	}

	@Operation(
		summary = "토론위키 수동 편집 리비전 추가",
		description = "ADMIN 이 직접 작성/수정한 본문을 새 리비전(source=ADMIN)으로 추가합니다(게시 안 함)."
	)
	@PostMapping("/revisions")
	public ApiResult<WikiRevisionResponse> addRevision(
		@Valid @RequestBody WikiManualRevisionRequest request,
		@AuthenticationPrincipal CustomUserDetails principal) {

		Long adminUserId = principal.getUserId();
		WikiRevisionResponse response = wikiGenerationService.addManualRevision(
			request.issueId(), request.content(), request.editSummary(), request.summary(), adminUserId);
		return ApiResult.success("수동 리비전을 추가했습니다.", response);
	}

	@Operation(
		summary = "AI 위키 초안 업로드",
		description = "서버 밖(로컬 claude -p 등)에서 생성한 초안을 source=AI DRAFT 리비전으로 저장합니다(게시 안 함). "
			+ "서비스 키로도 호출할 수 있습니다. 최신 리비전과 본문·요약이 같으면 새로 쌓지 않습니다."
	)
	@PostMapping("/drafts")
	public ApiResult<WikiRevisionResponse> uploadDraft(@Valid @RequestBody WikiDraftUploadRequest request) {

		WikiRevisionResponse response = wikiGenerationService.uploadDraft(
			request.issueId(), request.content(), request.summary(), request.model(), request.editSummary());
		return ApiResult.success("위키 초안을 저장했습니다.", response);
	}
}
