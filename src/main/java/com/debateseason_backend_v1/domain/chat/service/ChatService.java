package com.debateseason_backend_v1.domain.chat.service;

import com.debateseason_backend_v1.domain.chat.dto.ChatDTO;
import com.debateseason_backend_v1.domain.repository.entity.Chat;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.ChatRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;

    public ResponseEntity<?> saveChat(ChatDTO chatDTO,Long chatRoomId){

        // 1. chatRoom 조회하기
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(
                        ()-> new RuntimeException("There is no ChatRoom :"+chatRoomId)
                );



        // 2. Chat 엔티티 생성
        Chat chat = Chat.builder()
                .chatRoom(chatRoom)
                .sender(chatDTO.getSender())
                .category(chatDTO.getCategory())
                .content(chatDTO.getContent())
                .build();

        // 3. Chat 저장하기
        chatRepository.save(chat);

        return ResponseEntity.ok("Yes, Save Chat!");
    }

}
