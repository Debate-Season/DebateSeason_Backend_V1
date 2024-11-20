package com.debateseason_backend_v1.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserIssueDTO {
    private Long userId;
    private Long issueId;
}
