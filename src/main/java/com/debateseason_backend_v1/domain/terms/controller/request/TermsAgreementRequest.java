package com.debateseason_backend_v1.domain.terms.controller.request;

import java.util.List;

import com.debateseason_backend_v1.domain.terms.service.request.TermsAgreementServiceRequest;

public record TermsAgreementRequest(
	List<TermsAgreementItem> agreements
) {

	public TermsAgreementServiceRequest toServiceRequest(Long userId) {
		return TermsAgreementServiceRequest.builder()
			.userId(userId)
			.agreements(this.agreements)
			.build();
	}
}
