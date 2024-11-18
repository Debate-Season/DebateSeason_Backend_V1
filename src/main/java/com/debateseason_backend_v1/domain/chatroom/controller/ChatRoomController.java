package com.debateseason_backend_v1.domain.chatroom.controller;

import com.debateseason_backend_v1.domain.chat.dto.ChatDTO;
import com.debateseason_backend_v1.domain.chat.service.ChatService;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDTO;
import com.debateseason_backend_v1.domain.chatroom.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/room")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    // 4. 채팅방(=안건=토론방)생성하기, title,content -> JSON, issue_id = 쿼리스트링
    @PostMapping("/")
    public ResponseEntity<?> createChatRoom(@RequestBody ChatRoomDTO chatRoomDTO,
                                            @RequestParam(name = "issue-id") Long issue_id){
        return chatRoomService.saveChatRoom(chatRoomDTO,issue_id);
    }


    // 4. 채팅방 단건 불러오기
    @GetMapping("/")
    public ResponseEntity<?> getChatRoom(@RequestParam(name = "chatroom-id")Long chatRoomId){
        return chatRoomService.fetchChatRoom(chatRoomId);
    }

    // 5. 채팅방 찬성/반대 투표하기, opinion, chatroomid = 쿼리스트링
    @PostMapping("/vote")
    public ResponseEntity<?> voteChatRoom(@RequestParam(name = "opinion")String opinion,
                                          @RequestParam(name = "chatroom-id") Long chatRoomId){
        return chatRoomService.voteChatRoom(opinion,chatRoomId);
    }

    // 6. 채팅메시지 발송
    // 쿼리스트링은 chatRoomId
    @PostMapping("/send")
    public ResponseEntity<?> sendChat(@RequestBody ChatDTO chatDTO,
                                      @RequestParam(name="chatroom-id")Long chatRoomId){
        return chatService.saveChat(chatDTO,chatRoomId);
    }
}
