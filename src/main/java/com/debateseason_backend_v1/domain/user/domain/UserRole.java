package com.debateseason_backend_v1.domain.user.domain;

public enum UserRole {

	USER,
	ADMIN;

	// Spring Security 의 hasRole("ADMIN") 은 ROLE_ 접두사를 전제로 한다.
	public String getAuthority() {
		return "ROLE_" + name();
	}

	// 구버전 access token 에는 role 클레임이 없다. 그 경우 USER 로 간주한다.
	public static UserRole fromNullable(String name) {
		if (name == null || name.isBlank()) {
			return USER;
		}

		try {
			return valueOf(name);
		} catch (IllegalArgumentException e) {
			return USER;
		}
	}
}
