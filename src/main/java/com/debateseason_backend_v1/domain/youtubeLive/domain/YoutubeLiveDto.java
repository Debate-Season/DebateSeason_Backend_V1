package com.debateseason_backend_v1.domain.youtubeLive.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// Setter가 들어가는 순간 바뀔 수 있다.
// 그래서 도메인 엔티티에게 모든 책임을 위임한다.
@Getter
@Setter
@AllArgsConstructor
@Builder
public class YoutubeLiveDto {

	private Integer id;

	private String title;

	private String supplier;

	private String videoId;

	private String category;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime createAt;

	private String src;

}