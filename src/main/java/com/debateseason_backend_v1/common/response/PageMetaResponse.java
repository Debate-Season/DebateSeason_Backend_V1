package com.debateseason_backend_v1.common.response;

import org.springframework.data.domain.Page;

public record PageMetaResponse(
	int page,
	int size,
	long totalElements,
	int totalPages
) {

	public static PageMetaResponse of(Page<?> page) {
		
		return new PageMetaResponse(
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}
}