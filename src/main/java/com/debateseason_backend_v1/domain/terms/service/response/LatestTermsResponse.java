package com.debateseason_backend_v1.domain.terms.service.response;

import com.debateseason_backend_v1.domain.repository.entity.Terms;

public record LatestTermsResponse(
	Long termsId,
	String termsType,
	String notionUrl
) {

	public static LatestTermsResponse from(Terms terms) {

		return new LatestTermsResponse(
			terms.getId(),
			terms.getTermsType().name(),
			terms.getNotionUrl()
		);
	}

}
