package com.debateseason_backend_v1.domain.youtubeLive.domain;

import java.time.LocalDateTime;

import com.debateseason_backend_v1.crolling.youtubelive.domain.TmpYoutubeLiveDto;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import lombok.Setter;

@Getter
@Setter//?
@AllArgsConstructor
@Builder
public class YoutubeLive {
	private Integer id;

	private String title;

	private String supplier;

	private String videoId;

	private String category;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime createAt;

	private String src;

	public YoutubeLiveDto createDto() {

		// 새로운 객체 생성.
		return YoutubeLiveDto.builder()
			.id(id)
			.title(title)
			.supplier(supplier)
			.videoId(videoId)
			.category(category)
			.createAt(createAt)
			.src(src)
			.build()
			;

	}


}