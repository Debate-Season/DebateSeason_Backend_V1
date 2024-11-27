package com.debateseason_backend_v1.domain.issue.controller;

import com.debateseason_backend_v1.domain.issue.dto.IssueDTO;
import com.debateseason_backend_v1.domain.issue.service.IssueServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// ADMIN 전용 Controller
// 여기서 ADMIN이 ISSUE등록/삭제/수정 다할듯.
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class AdminIssueControllerV1 {


    private final IssueServiceV1 issueServiceV1;

    // 1. 이슈방 만들기
    @PostMapping("/issue")
    public ResponseEntity<?> saveIssue(@RequestBody IssueDTO issueDTO){
        return issueServiceV1.save(issueDTO);
    }

}
