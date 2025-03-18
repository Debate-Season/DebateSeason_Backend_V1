package com.debateseason_backend_v1.domain.user.component.provider;

public interface OidcProvider {

	/**
	 * idToken 검증 후,
	 * 사용자 식별자(sub)를 반환.
	 * 검증 실패 시 예외 발생.
	 */
	String extractUserId(String idToken);
}
