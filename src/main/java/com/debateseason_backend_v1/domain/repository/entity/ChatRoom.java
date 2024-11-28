package com.debateseason_backend_v1.domain.repository.entity;

import jakarta.persistence.*;
import lombok.*;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class ChatRoom {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private Issue issue;

    private String title;
    private String content;

    private int yes;

    private int no;
}
