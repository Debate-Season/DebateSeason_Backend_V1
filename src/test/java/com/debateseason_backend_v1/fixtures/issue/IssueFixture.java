package com.debateseason_backend_v1.fixtures.issue;

import com.debateseason_backend_v1.domain.repository.entity.Issue;

import java.time.LocalDateTime;

public class IssueFixture {

    public static Issue create() {
        return Issue.builder()
                .id(1L)
                .title("title")
                .majorCategory("major category")
                .middleCategory("middle category")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
