package com.debateseason_backend_v1.domain.youtubeLive.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
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


}