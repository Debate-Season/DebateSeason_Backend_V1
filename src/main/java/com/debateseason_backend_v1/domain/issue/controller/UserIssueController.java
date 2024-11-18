package com.debateseason_backend_v1.domain.issue.controller;

import com.debateseason_backend_v1.domain.issue.service.IssueService;
import com.debateseason_backend_v1.domain.user.dto.UserIssueDTO;
import com.debateseason_backend_v1.domain.user.servcie.UserIssueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/issue")
public class UserIssueController {


    private final IssueService issueService;
    private final UserIssueService userIssueService;

    // 2. 이슈방 단건 불러오기(+ 채팅방도 같이 불러와야 함.)
    @GetMapping("/")
    public ResponseEntity<?> getIssue(@RequestParam(name = "issue-id") Long issueId) throws JsonProcessingException {
        return issueService.fetchIssue(issueId);
    }

    // 3. 이슈방 즐겨찾기. Param = name(User 이름), title(Issue 제목)
    @PostMapping("/sub")
    public ResponseEntity<?> subscribeIssue(@RequestBody UserIssueDTO userIssueDTO){
        return userIssueService.saveUserIssue(userIssueDTO);
    }

}