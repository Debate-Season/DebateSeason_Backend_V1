package com.debateseason_backend_v1.domain.issue.presentation.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.issue.application.service.IssueStatusService;
import com.debateseason_backend_v1.domain.issue.model.request.IssueStatusChangeRequest;
import com.debateseason_backend_v1.domain.issue.model.response.IssueStatusChangeResponse;
import com.debateseason_backend_v1.security.service.Actor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 이슈 상태 변경 — 잠재우기·깨우기(PUBLISHED ↔ DORMANT), 신설 롤백(→ ARCHIVED).
 *
 * ADMIN JWT 또는 서비스 API 키({@code X-Service-Key})로 호출한다.
 * 데이터 파이프라인이 Discord 에서 승인된 제안을 반영할 때 쓴다. 나머지 반영 API 는 OntologyAdminController.
 */
@Tag(name = "Issue Admin API", description = "이슈 상태 변경 — ADMIN 또는 서비스 키")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/issue")
public class IssueStatusAdminController {

	private final IssueStatusService issueStatusService;

	@Operation(
		summary = "이슈 상태 변경 (잠재우기·깨우기·롤백)",
		description = "PUBLISHED·DORMANT·ARCHIVED 사이만 허용합니다. ARCHIVED 는 앱에서 사라집니다(데이터 보존). "
			+ "이미 그 상태면 changed=false 로 200 을 돌려주고 이력을 남기지 않습니다. DRAFT 는 409 입니다."
	)
	@PatchMapping("/{issueId}/status")
	public ApiResult<IssueStatusChangeResponse> changeStatus(
		@PathVariable Long issueId,
		@Valid @RequestBody IssueStatusChangeRequest request,
		Authentication authentication) {

		IssueStatusChangeResponse response = issueStatusService.changeStatus(
			issueId, request.status(), request.reason(), request.approver(), Actor.of(authentication));
		return ApiResult.success("이슈 상태를 반영했습니다.", response);
	}
}
