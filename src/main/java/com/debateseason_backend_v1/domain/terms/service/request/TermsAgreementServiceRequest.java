package com.debateseason_backend_v1.domain.terms.service.request;

import java.util.List;

import com.debateseason_backend_v1.domain.terms.controller.request.TermsAgreementItem;

import lombok.Builder;

@Builder
public record TermsAgreementServiceRequest(
	List<TermsAgreementItem> agreements,
	Long userId
) {
}
