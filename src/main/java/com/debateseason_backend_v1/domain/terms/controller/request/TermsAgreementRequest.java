package com.debateseason_backend_v1.domain.terms.controller.request;

import java.util.List;

import com.debateseason_backend_v1.domain.terms.service.request.TermsAgreementServiceRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "이용약관 동의 요청 DTO", description = "이용약관 동의 요청")
public record TermsAgreementRequest(
	@Schema(description = "이용약관 동의 목록 DTO")
	List<TermsAgreementItem> agreements
) {

	public TermsAgreementServiceRequest toServiceRequest(Long userId) {
		return TermsAgreementServiceRequest.builder()
			.userId(userId)
			.agreements(this.agreements)
			.build();
	}
}
