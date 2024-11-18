package com.debateseason_backend_v1.domain.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Issue {

    public Issue(){

    }

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "issue_id")
    private long id;

    private String title;


}
