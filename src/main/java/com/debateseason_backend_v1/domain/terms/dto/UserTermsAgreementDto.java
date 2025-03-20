package com.debateseason_backend_v1.domain.terms.dto;

import java.time.LocalDateTime;

import com.debateseason_backend_v1.domain.terms.enums.TermsType;

public record UserTermsAgreementDto(
	TermsType termsType,
	LocalDateTime agreedAt
) {
}