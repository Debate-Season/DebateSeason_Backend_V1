package com.debateseason_backend_v1.security.service;

import org.springframework.security.core.Authentication;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.security.CustomUserDetails;

/**
 * 관리 변경 이력의 decided_by 표기. 인가를 통과했다면 서비스 키 아니면 ADMIN JWT 다.
 */
public final class Actor {

	private Actor() {
	}

	public static String of(Authentication authentication) {
		Object principal = authentication == null ? null : authentication.getPrincipal();
		if (principal instanceof ServicePrincipal service) {
			return service.actor();
		}
		if (principal instanceof CustomUserDetails user) {
			return "user:" + user.getUserId();
		}
		throw new CustomException(ErrorCode.ACCESS_DENIED);
	}
}
