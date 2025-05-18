package com.debateseason_backend_v1.domain.user.component;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component
public class SystemTimeProvider {
	
	public LocalDateTime now() {
		return LocalDateTime.now();
	}
}