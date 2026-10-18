package com.debateseason_backend_v1.domain.ontology.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.ontology.model.request.DebateTopicCreateRequest;
import com.debateseason_backend_v1.domain.ontology.model.request.EventLinkRequest;
import com.debateseason_backend_v1.domain.ontology.model.request.EventUnlinkRequest;
import com.debateseason_backend_v1.domain.ontology.model.request.IssueCreateRequest;
import com.debateseason_backend_v1.domain.ontology.model.request.IssueRenameRequest;
import com.debateseason_backend_v1.domain.ontology.model.request.TopicStatusChangeRequest;
import com.debateseason_backend_v1.domain.ontology.model.response.DebateTopicCreateResponse;
import com.debateseason_backend_v1.domain.ontology.model.response.EventLinkResponse;
import com.debateseason_backend_v1.domain.ontology.model.response.EventUnlinkResponse;
import com.debateseason_backend_v1.domain.ontology.model.response.IssueCreateResponse;
import com.debateseason_backend_v1.domain.ontology.model.response.IssueRenameResponse;
import com.debateseason_backend_v1.domain.ontology.model.response.TopicStatusChangeResponse;
import com.debateseason_backend_v1.domain.ontology.service.OntologyAdminService;
import com.debateseason_backend_v1.security.service.Actor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 온톨로지 제안의 승인(✅)·롤백(❌) 반영. ADMIN JWT 또는 서비스 API 키로 호출한다.
 *
 * <pre>
 * 제안          반영(✅)                                   롤백(❌)
 * 이슈 신설      POST  /admin/issue                         PATCH /admin/issue/{id}/status ARCHIVED
 * 이슈명 변경    PATCH /admin/issue/{id}                    PATCH /admin/issue/{id}  (previousTitle)
 * 잠재우기       PATCH /admin/issue/{id}/status DORMANT      PATCH /admin/issue/{id}/status PUBLISHED
 * 토론 주제      POST  /admin/issue/{id}/debate-topic        PATCH /admin/room/{id}/status HIDDEN
 * 사건 추가      POST  /admin/issue/{id}/events              DELETE /admin/issue/{id}/events
 * </pre>
 */
@Tag(name = "Ontology Admin API", description = "온톨로지 승인 반영·롤백 — ADMIN 또는 서비스 키")
@RequiredArgsConstructor
@RestController
public class OntologyAdminController {

	private final OntologyAdminService ontologyAdminService;

	@Operation(summary = "이슈 신설", description = "PUBLISHED 이슈와 컨테이너 방을 함께 만듭니다. 같은 proposalId 재호출은 처음 결과(created=false).")
	@PostMapping("/api/v1/admin/issue")
	public ApiResult<IssueCreateResponse> createIssue(
		@Valid @RequestBody IssueCreateRequest request, Authentication authentication) {

		return ApiResult.success("이슈를 만들었습니다.", ontologyAdminService.createIssue(request, Actor.of(authentication)));
	}

	@Operation(summary = "이슈명 변경", description = "컨테이너 방 이름도 같이 바꿉니다. 응답의 previousTitle 로 다시 부르면 롤백입니다.")
	@PatchMapping("/api/v1/admin/issue/{issueId}")
	public ApiResult<IssueRenameResponse> renameIssue(
		@PathVariable Long issueId, @Valid @RequestBody IssueRenameRequest request, Authentication authentication) {

		return ApiResult.success("이슈명을 반영했습니다.",
			ontologyAdminService.renameIssue(issueId, request, Actor.of(authentication)));
	}

	@Operation(summary = "토론 주제 신설", description = "이슈 컨테이너 아래 OPEN 스레드로 만듭니다. 컨테이너가 없으면 함께 만듭니다.")
	@PostMapping("/api/v1/admin/issue/{issueId}/debate-topic")
	public ApiResult<DebateTopicCreateResponse> createDebateTopic(
		@PathVariable Long issueId, @Valid @RequestBody DebateTopicCreateRequest request, Authentication authentication) {

		return ApiResult.success("토론 주제를 만들었습니다.",
			ontologyAdminService.createDebateTopic(issueId, request, Actor.of(authentication)));
	}

	@Operation(summary = "토론 주제 상태 변경", description = "스레드의 OPEN ↔ HIDDEN 만 허용합니다. HIDDEN 은 목록·랭킹에서 빠집니다. 컨테이너는 409.")
	@PatchMapping("/api/v1/admin/room/{chatRoomId}/status")
	public ApiResult<TopicStatusChangeResponse> changeTopicStatus(
		@PathVariable Long chatRoomId, @Valid @RequestBody TopicStatusChangeRequest request, Authentication authentication) {

		return ApiResult.success("토론 주제 상태를 반영했습니다.",
			ontologyAdminService.changeTopicStatus(chatRoomId, request, Actor.of(authentication)));
	}

	@Operation(summary = "사건 추가", description = "사건을 만들고 이슈에 연결합니다. mediaIds 가 하나라도 없으면 404 이고 아무것도 저장하지 않습니다.")
	@PostMapping("/api/v1/admin/issue/{issueId}/events")
	public ApiResult<EventLinkResponse> linkEvents(
		@PathVariable Long issueId, @Valid @RequestBody EventLinkRequest request, Authentication authentication) {

		return ApiResult.success("사건을 연결했습니다.",
			ontologyAdminService.linkEvents(issueId, request, Actor.of(authentication)));
	}

	@Operation(summary = "사건 연결 해제", description = "사건 추가의 롤백입니다. 연결만 끊고 사건 행은 남깁니다.")
	@DeleteMapping("/api/v1/admin/issue/{issueId}/events")
	public ApiResult<EventUnlinkResponse> unlinkEvents(
		@PathVariable Long issueId, @Valid @RequestBody EventUnlinkRequest request, Authentication authentication) {

		return ApiResult.success("사건 연결을 해제했습니다.",
			ontologyAdminService.unlinkEvents(issueId, request, Actor.of(authentication)));
	}
}
