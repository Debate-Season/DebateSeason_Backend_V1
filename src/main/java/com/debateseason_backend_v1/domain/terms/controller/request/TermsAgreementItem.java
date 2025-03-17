package com.debateseason_backend_v1.domain.terms.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "이용약관 동의 항목 DTO", description = "동의 항목")
public record TermsAgreementItem(
	@Schema(description = "이용약관 ID", example = "1")
	Long termsId,

	@Schema(description = "동의 여부", example = "true")
	boolean agreed
) {
}
