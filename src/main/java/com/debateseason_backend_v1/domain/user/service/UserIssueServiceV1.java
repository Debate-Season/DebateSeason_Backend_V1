package com.debateseason_backend_v1.domain.user.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.domain.repository.IssueRepository;
import com.debateseason_backend_v1.domain.repository.UserIssueRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.repository.entity.UserIssue;
import com.debateseason_backend_v1.domain.user.dto.UserIssueDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserIssueServiceV1 {
	// User와 Issue를 조회한다, -> UserIssue에 등록을 한다.

	//
	private final UserRepository userRepository;
	private final IssueRepository issueRepository;
	//
	private final UserIssueRepository userIssueRepository;

	// 1. userIssue에 저장하기
	public ResponseEntity<?> save(UserIssueDTO userIssueDTO) {

		Long userId = userIssueDTO.getUserId();
		Long issueId = userIssueDTO.getIssueId();

		// 이 부분 나중에 GlobalExceptionHandler 만들어서 예외처리 할 예정 <- 수정해야 할 부분
		User user = userRepository.findById(userId).orElseThrow(
			() -> new RuntimeException("There is no user : " + userId)
		);
		Issue issue = issueRepository.findById(issueId).orElseThrow(
			() -> new RuntimeException("There is no relevant issue : " + issueId)
		);

		UserIssue userIssue = UserIssue.builder()
			.user(user)
			.issue(issue)
			.build();

		userIssueRepository.save(userIssue);

		return ResponseEntity.ok("UserIssue를 성공적을 저장했습니다.");
	}

}
