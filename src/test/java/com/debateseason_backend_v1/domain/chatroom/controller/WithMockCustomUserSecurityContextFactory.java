package com.debateseason_backend_v1.domain.chatroom.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.debateseason_backend_v1.security.CustomUserDetails;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
	@Override
	public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
		final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

		CustomUserDetails principal = CustomUserDetails.from(1L);

		// 비밀번호 설정 안함.
		Authentication authentication =
			new UsernamePasswordAuthenticationToken(
				principal,
				null,
				principal.getAuthorities()
			);

		securityContext.setAuthentication(authentication);
		return securityContext;
	}
}
