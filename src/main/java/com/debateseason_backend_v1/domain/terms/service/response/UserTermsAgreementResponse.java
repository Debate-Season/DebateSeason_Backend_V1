package com.debateseason_backend_v1.domain.terms.service.response;

import java.time.LocalDateTime;

import com.debateseason_backend_v1.domain.terms.enums.TermsType;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record UserTermsAgreementResponse(
	TermsType termsType,

	@JsonFormat(pattern = "yyyy.MM.dd")
	LocalDateTime agreedAt,
	String notionUrl
) {

	public static UserTermsAgreementResponse of(
		TermsType termsType,
		LocalDateTime agreedAt,
		String notionUrl
	) {
		return UserTermsAgreementResponse.builder()
			.termsType(termsType)
			.agreedAt(agreedAt)
			.notionUrl(notionUrl)
			.build();
	}

}