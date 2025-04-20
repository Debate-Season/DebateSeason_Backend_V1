package com.debateseason_backend_v1.media.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BreakingNewsResponse {
	@Schema(description = "title", example = "국민 세금으로 국정운영하라고 국회의원 세워놨더니")
	private String title; // 제목

	@Schema(description = "url", example = "https://bbs.ruliweb.com/best/board/300148/read/37899929?m=political&t=now")
	private String url; // href
}
