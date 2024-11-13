package com.debateseason_backend_v1.domain.user.controller;



import com.debateseason_backend_v1.domain.chat.dto.ChatDTO;
import com.debateseason_backend_v1.domain.chat.service.ChatService;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDTO;
import com.debateseason_backend_v1.domain.chatroom.model.ChatRoom;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomService;
import com.debateseason_backend_v1.domain.issue.model.Issue;
import com.debateseason_backend_v1.domain.issue.service.IssueService;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;
import com.debateseason_backend_v1.domain.user.dto.UserIssueDTO;
import com.debateseason_backend_v1.domain.user.servcie.UserIssueService;
import com.debateseason_backend_v1.domain.user.servcie.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
public class UserController {

    private final UserService userService;
    private final UserIssueService userIssueService;
    private final IssueService issueService;

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    private final ObjectMapper objectMapper;

    // 1. 회원가입하기(임시)
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserDTO userDTO){
        return userService.saveUser(userDTO);

    }

    // 2. 이슈방 단건 불러오기(+ 채팅방도 같이 불러와야 함.)
    @GetMapping("/issue")
    public ResponseEntity<?> getIssue(@RequestParam(name = "issue-id") Long issueId) throws JsonProcessingException {
        return issueService.fetchIssue(issueId);
    }

    // 3. 이슈방 즐겨찾기. Param = name(User 이름), title(Issue 제목)
    @PostMapping("/issue/sub")
    public ResponseEntity<?> subscribeIssue(@RequestBody UserIssueDTO userIssueDTO){
        return userIssueService.saveUserIssue(userIssueDTO);
    }

    // 4. 채팅방(=안건=토론방)생성하기, title,content -> JSON, issue_id = 쿼리스트링
    @PostMapping("/issue/chatroom")
    public ResponseEntity<?> createChatRoom(@RequestBody ChatRoomDTO chatRoomDTO,
                                            @RequestParam(name = "issue-id") Long issue_id){
        return chatRoomService.saveChatRoom(chatRoomDTO,issue_id);
    }

    
    // 4. 채팅방 단건 불러오기
    @GetMapping("/issue/chatroom")
    public ResponseEntity<?> getChatRoom(@RequestParam(name = "chatroom-id")Long chatRoomId){
        return chatRoomService.fetchChatRoom(chatRoomId);
    }
    
    // 5. 채팅방 찬성/반대 투표하기, opinion, chatroomid = 쿼리스트링
    @PostMapping("/issue/chatroom/vote")
    public ResponseEntity<?> voteChatRoom(@RequestParam(name = "opinion")String opinion,
                                          @RequestParam(name = "chatroom-id") Long chatRoomId){
        return chatRoomService.voteChatRoom(opinion,chatRoomId);
    }

    // 6. 채팅메시지 발송
    // 쿼리스트링은 chatRoomId
    @PostMapping("/issue/chatroom/send")
    public ResponseEntity<?> sendChat(@RequestBody ChatDTO chatDTO,
                                      @RequestParam(name="chatroom-id")Long chatRoomId){
        return chatService.saveChat(chatDTO,chatRoomId);
    }

}
