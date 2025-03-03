package com.debateseason_backend_v1.domain.issue.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueDTO {

    @Schema(description = "이슈방 제목",example = "스테그플레이션 위기")
    private String title;

    @Schema(description = "대분류",example = "사회")
    private String majorCategory;

    //private String middleCategory;
}
