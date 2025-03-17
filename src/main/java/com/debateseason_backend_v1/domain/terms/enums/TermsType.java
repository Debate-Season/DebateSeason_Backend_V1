package com.debateseason_backend_v1.domain.terms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsType {

	// 필수 약관
	SERVICE(true, "서비스 이용약관"),
	PRIVACY(true, "개인정보 처리방침");

	private final boolean required;
	private final String displayName;
	
}