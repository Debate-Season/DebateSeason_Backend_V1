package com.debateseason_backend_v1.domain.media.model.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaResponse {

	@Schema(description = "id", example = "1")
	private Long id;

	@Schema(description = "제목", example = "JENNIE - like JENNIE (Official Video)")
	private String title;
	
	@Schema(description = "공급업체", example = "중앙일보")
	private String supplier;

	@Schema(description = "시간",example = "2024-12-03T08:51:57")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime outdated;

	@Schema(description = "url",example = "https://n.news.naver.com/article/052/0002162828?sid=100")
	private String url;

	@Schema(description = "src",example = "https://mimgnews.pstatic.net/image/origin/052/2025/03/29/2172771.jpg?type=nf212_140&ut=1743244864000")
	private String src;
}
