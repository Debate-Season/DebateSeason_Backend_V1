package com.debateseason_backend_v1.domain.chat.model;

import com.debateseason_backend_v1.domain.chatroom.model.ChatRoom;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Entity
public class Chat {

    public Chat(){

    }

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private ChatRoom chatRoom;

    // 발신자
    private String sender;
    // 소속 커뮤니티
    private String category;
    private String content;
}
