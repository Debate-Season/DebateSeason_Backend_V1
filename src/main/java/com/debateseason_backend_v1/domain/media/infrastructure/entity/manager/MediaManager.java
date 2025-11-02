package com.debateseason_backend_v1.domain.media.infrastructure.entity.manager;

import java.util.List;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.media.application.repository.MediaRepository;
import com.debateseason_backend_v1.domain.media.model.response.BreakingNewsResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MediaManager { // 싱글톤으로 등록.

	private final MediaRepository mediaRepository;

	// 1. 속보 가져오기
	public List<BreakingNewsResponse> findTop10BreakingNews(){

		// 매번 새로운 객체를 반환한다.
		return mediaRepository.findTop10BreakingNews().stream()
			.map(
				e->
					BreakingNewsResponse.builder()
						.title(e.getTitle())
						.url(e.getUrl())
						.build()
			).toList();
	}


}
