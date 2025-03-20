package com.debateseason_backend_v1.domain.terms.service.response;

import java.time.LocalDateTime;

import com.debateseason_backend_v1.domain.terms.enums.TermsType;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "내가 동의한 이용약관 목록 조회 응답 DTO", description = "내가 동의한 이용약관 목록 응답")
public record UserTermsAgreementResponse(
	@Schema(description = "이용약관 타입", example = "SERVICE")
	TermsType termsType,

	@JsonFormat(pattern = "yyyy.MM.dd")
	@Schema(description = "동의일자", example = "2025.03.19")
	LocalDateTime agreedAt,

	@Schema(description = "노션 URL", example = "https://hurricane-ticket-d3c.notion-5.site/18d0...")
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