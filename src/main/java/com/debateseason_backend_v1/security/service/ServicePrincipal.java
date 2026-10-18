package com.debateseason_backend_v1.security.service;

/**
 * 서비스 API 키로 인증된 호출자. 사용자 id 가 없으므로 {@code CustomUserDetails} 와 구분한다.
 *
 * @param name 키 설정의 이름 (예: ontology)
 */
public record ServicePrincipal(String name) {

	public static final String ROLE = "SERVICE";

	/** 이력에 남는 요청 주체 표기. */
	public String actor() {
		return "service:" + name;
	}

	@Override
	public String toString() {
		return actor();
	}
}
