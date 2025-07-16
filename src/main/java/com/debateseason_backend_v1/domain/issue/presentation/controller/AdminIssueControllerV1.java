package com.debateseason_backend_v1.domain.issue.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.issue.model.request.IssueRequest;
import com.debateseason_backend_v1.domain.issue.application.service.IssueServiceV1;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

// ADMIN 전용 Controller
// 여기서 ADMIN이 ISSUE등록/삭제/수정 다할듯.
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class AdminIssueControllerV1 {

	private final IssueServiceV1 issueServiceV1;
	// 1. 이슈방 만들기
	@Operation(
		summary = "이슈방을 만듭니다(ADMIN)",
		description = " ")
	@PostMapping("/issue")
	public ApiResult<Object> saveIssue(@RequestBody IssueRequest issueRequest) {
		return issueServiceV1.save(issueRequest);
	}

}
