package com.debateseason_backend_v1.domain.user.domain;

public record UserId(
	Long value

) {
	public static final UserId EMPTY = new UserId(null);
}
