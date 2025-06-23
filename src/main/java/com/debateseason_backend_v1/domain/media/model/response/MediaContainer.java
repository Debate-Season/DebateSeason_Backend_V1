package com.debateseason_backend_v1.domain.media.model.response;

import java.util.List;
import java.util.Map;

import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLive;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaContainer {

	private Map<String,YoutubeLive> youtubeLiveContainer;

	private List<MediaResponse> items;
}
