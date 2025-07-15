package com.debateseason_backend_v1.fixtures.issue;

import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;

import java.time.LocalDateTime;

public class IssueFixture {

    public static IssueEntity create() {
        return IssueEntity.builder()
                .id(1L)
                .title("title")
                .majorCategory("major category")
                .middleCategory("middle category")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
