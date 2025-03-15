package com.debateseason_backend_v1.domain.terms.service.response;

import com.debateseason_backend_v1.domain.repository.entity.Terms;

import lombok.Builder;

@Builder
public record LatestTermsResponse(
	Long termsId,
	String termsType,
	String version,
	String notionUrl
) {

	public static LatestTermsResponse from(Terms terms) {

		return LatestTermsResponse.builder()
			.termsId(terms.getId())
			.termsType(terms.getTermsType().name())
			.version(terms.getVersion())
			.notionUrl(terms.getNotionUrl())
			.build();
	}

}
