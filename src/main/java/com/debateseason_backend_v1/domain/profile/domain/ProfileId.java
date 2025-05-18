package com.debateseason_backend_v1.domain.profile.domain;

public record ProfileId(
	Long value
) {
	public static final ProfileId EMPTY = new ProfileId(null);
}
