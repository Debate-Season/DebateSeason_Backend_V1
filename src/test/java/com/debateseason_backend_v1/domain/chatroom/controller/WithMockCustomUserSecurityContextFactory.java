package com.debateseason_backend_v1.domain.chatroom.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
	@Override
	public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
		final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

		//CustomUserDetails userDetails = new CustomUserDetails(); <- private final이라서 CustomUserDetails를 만들 수가 없어서 테스트가 x

		//final UsernamePasswordAuthenticationToken authenticationToken
		//	= new UsernamePasswordAuthenticationToken(userDetails,"1234",null);
		//Arrays.asList(new SimpleGrantedAuthority(annotation.grade()))

		//securityContext.setAuthentication(authenticationToken);
		return securityContext;
	}
}
