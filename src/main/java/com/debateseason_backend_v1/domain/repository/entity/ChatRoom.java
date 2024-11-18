package com.debateseason_backend_v1.domain.repository.entity;

import com.debateseason_backend_v1.domain.repository.entity.Issue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Entity
public class ChatRoom {

    public ChatRoom(){

    }

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private Issue issue;

    private String title;
    private String content;

    private int yes;

    private int no;
}
