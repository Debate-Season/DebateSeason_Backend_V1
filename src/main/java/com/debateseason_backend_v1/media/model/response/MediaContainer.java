package com.debateseason_backend_v1.media.model.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaContainer {

	private List<MediaResponse> items;
}
