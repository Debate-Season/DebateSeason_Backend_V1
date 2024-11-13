package com.debateseason_backend_v1.domain.chatroom.service;

import com.debateseason_backend_v1.domain.chat.dto.ChatDAO;
import com.debateseason_backend_v1.domain.chat.dto.ResponseDTO;
import com.debateseason_backend_v1.domain.chat.model.Chat;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDTO;
import com.debateseason_backend_v1.domain.chatroom.model.ChatRoom;
import com.debateseason_backend_v1.domain.issue.model.Issue;
import com.debateseason_backend_v1.domain.repository.ChatRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.IssueRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final IssueRepository issueRepository; // 혹시나 Service쓰면, 나중에 순환참조 발생할 것 같아서 Repository로 함.
    private final ChatRepository chatRepository;

    private final ObjectMapper objectMapper;

    // 1. 채팅방 저장하기
    public ResponseEntity<?> saveChatRoom(ChatRoomDTO chatRoomDTO,long issueId){

        // 1. Issue 찾기
        Issue issue = issueRepository.findById(issueId).orElseThrow(
                ()-> new RuntimeException("There is no "+issueId)
        );


        // 2 ChatRoom 엔티티 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .issue(issue)
                .title(chatRoomDTO.getTitle())
                .content(chatRoomDTO.getContent())
                .yes(0)
                .no(0)
                .build()
                ;

        // 3. save ChatRoom
        chatRoomRepository.save(chatRoom);

        return ResponseEntity.ok("Successfully make ChatRoom!");
    }

    // 2. 채팅방 찬반 투표하기
    // Dirty Checking을 위해서 Transactional을 통한 변경감지
    @Transactional
    public ResponseEntity<?> voteChatRoom(String opinion,Long chatRoomId){

        //1. 채팅방 가져오기
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(
                ()-> new RuntimeException("Cannot vote! : "+chatRoomId)
        );

        //2. 변경사항 반영하기
        if(opinion.equals("yes")){
            long countYes = chatRoom.getYes();
            chatRoom.setYes((int) (countYes+1));
        }
        else{
            long countNo = chatRoom.getNo();
            chatRoom.setNo((int) (countNo+1));
        }

        return ResponseEntity.ok("Vote Successfully");
    }

    // 3. 채팅방 단건 불러오기
    public ResponseEntity<?> fetchChatRoom(Long chatRoomId){
        
        // 1. 채팅방 불러오기
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(
                        ()-> new RuntimeException("There is no ChatRoom: "+chatRoomId)
                )
                ;

        // 2. 관련 채팅들 불러오기
        List<Chat> origChatList = chatRepository.findByChatRoom(chatRoom);


        List<ChatDAO> modifiedChatList = new ArrayList<>();

        for(Chat c:origChatList){
            ChatDAO chatDAO = ChatDAO.builder()
                    .sender(c.getSender())
                    .category(c.getCategory())
                    .content(c.getContent())
                    .build()
                    ;

            modifiedChatList.add(chatDAO);
        }

        ResponseDTO responseDTO = ResponseDTO.builder()
                .chatRoom(chatRoom)
                .chatList(modifiedChatList)
                .build()
                ;

        String json;

        try {
            json = objectMapper.writeValueAsString(responseDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(json);

    }
}
