package com.debateseason_backend_v1.domain.issue.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.debateseason_backend_v1.domain.issue.service.IssueServiceV1;
import com.debateseason_backend_v1.domain.user.servcie.UserIssueServiceV1;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserIssueControllerV1 {

	private final IssueServiceV1 issueServiceV1;
	private final UserIssueServiceV1 userIssueServiceV1;

	// 2. 이슈방 단건 불러오기(+ 채팅방도 같이 불러와야 함.)
	@Operation(
		summary = "이슈방 단건 불러오기",
		description = "이슈방 상세보기(+ 채팅방도 같이 불러와야 함.)")
	@GetMapping("/issue")
	public ResponseEntity<?> getIssue(@RequestParam(name = "issue-id") Long issueId, Long userId) {
		return issueServiceV1.fetch(issueId, userId);
	}

	// 3. 이슈방 즐겨찾기. body = { userid:{?}, issueid:{?}} JSON
	// -> 추후 추가될 여지가 있으므로, 일단 보류
	/*
	@Operation(
		summary = "이슈방 즐겨찾기",
		description = "body = { userid:{?}, issueid:{?}} JSON")
	@PostMapping("/issue/sub")
	public ResponseEntity<?> subscribeIssue(@RequestBody UserIssueDTO userIssueDTO) {
		return userIssueServiceV1.save(userIssueDTO);
	}
	 */

}