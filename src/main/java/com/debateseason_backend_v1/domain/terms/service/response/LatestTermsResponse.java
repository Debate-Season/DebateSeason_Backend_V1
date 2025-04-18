package com.debateseason_backend_v1.domain.terms.service.response;

import com.debateseason_backend_v1.domain.repository.entity.Terms;
import com.debateseason_backend_v1.domain.terms.enums.TermsType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "이용약관 조회 응답 DTO", description = "이용약관 조회 응답")
public record LatestTermsResponse(
	@Schema(title = "이용약관 ID", example = "1")
	Long termsId,

	@Schema(title = "이용약관 종류", example = "SERVICE")
	TermsType termsType,

	@Schema(title = "이용약관 버전", example = "1.0.0")
	String version,

	@Schema(title = "이용약관 노션 URL", example = "https://hurricane-ticket-d3c.notion.site/18d0...")
	String notionUrl
) {

	public static LatestTermsResponse from(Terms terms) {

		return LatestTermsResponse.builder()
			.termsId(terms.getId())
			.termsType(terms.getTermsType())
			.version(terms.getVersion())
			.notionUrl(terms.getNotionUrl())
			.build();
	}

}
