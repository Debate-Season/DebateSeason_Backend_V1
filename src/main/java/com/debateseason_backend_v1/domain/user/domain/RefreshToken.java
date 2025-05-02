package com.debateseason_backend_v1.domain.user.domain;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshToken {

	private Long id;
	private UserId userId;
	private String token;
	private LocalDateTime expirationAt;
	private LocalDateTime createdAt;

}
