package com.debateseason_backend_v1.domain.user.domain.rules;

public interface Rule<T> {
	void check(T target);
}