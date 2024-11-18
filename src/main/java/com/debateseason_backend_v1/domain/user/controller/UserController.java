package com.debateseason_backend_v1.domain.user.controller;



import com.debateseason_backend_v1.domain.chat.dto.ChatDTO;
import com.debateseason_backend_v1.domain.chat.service.ChatService;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDTO;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomService;
import com.debateseason_backend_v1.domain.issue.service.IssueService;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;
import com.debateseason_backend_v1.domain.user.dto.UserIssueDTO;
import com.debateseason_backend_v1.domain.user.servcie.UserIssueService;
import com.debateseason_backend_v1.domain.user.servcie.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    // 1. 회원가입하기(임시)
    // JSON 타입으로 데이터를 받는다.
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserDTO userDTO){
        return userService.saveUser(userDTO);

    }

}
