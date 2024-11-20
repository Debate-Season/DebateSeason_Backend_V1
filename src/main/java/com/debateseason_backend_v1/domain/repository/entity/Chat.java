package com.debateseason_backend_v1.domain.repository.entity;

import jakarta.persistence.*;
import lombok.*;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Chat {


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
