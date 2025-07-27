package com.debateseason_backend_v1.domain.media.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


// 불변 필드로 함으로써, 일관성 및 안정성 보장
// GC 스캔 범위도 줄일 수 있음.
@Getter
@AllArgsConstructor
@Builder
public class Media {

	private final Long id;

	private final String title;

	private final String url;

	private final String src;

	private final String category;

	private final String media;

	private final String type;// news, community, youtube

	private final int count;// 조회수

	private final LocalDateTime createdAt;
}
