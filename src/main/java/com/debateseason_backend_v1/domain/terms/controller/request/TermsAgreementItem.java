package com.debateseason_backend_v1.domain.terms.controller.request;

public record TermsAgreementItem(
	Long termsId,
	boolean agreed
) {
}
