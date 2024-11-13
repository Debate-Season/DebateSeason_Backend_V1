package com.debateseason_backend_v1.domain.user.model;

import com.debateseason_backend_v1.domain.issue.model.Issue;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class UserIssue {

    public UserIssue(){

    }

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "issue_id")
    private Issue issue;
}
