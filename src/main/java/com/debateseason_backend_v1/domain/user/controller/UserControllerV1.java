package com.debateseason_backend_v1.domain.user.controller;




import com.debateseason_backend_v1.domain.issue.service.IssueServiceV1;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;
import com.debateseason_backend_v1.domain.user.servcie.UserServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserControllerV1 {

    private final UserServiceV1 userServiceV1;
    private final IssueServiceV1 issueServiceV1;

    // 1. 회원가입하기(임시)
    // JSON 타입으로 데이터를 받는다.
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserDTO userDTO){
        return userServiceV1.save(userDTO);

    }

    // 2. 인덱스 페이지(홈)
    // 이슈방 전체 나열
    @GetMapping("")
    public ResponseEntity<?> indexPage(){
        return issueServiceV1.fetchAll();
    }

}
