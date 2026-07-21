package com.debateseason_backend_v1.domain.chatroom.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

import com.debateseason_backend_v1.domain.user.domain.UserRole;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

	long userId() default 1L;

	UserRole role() default UserRole.USER;
}
