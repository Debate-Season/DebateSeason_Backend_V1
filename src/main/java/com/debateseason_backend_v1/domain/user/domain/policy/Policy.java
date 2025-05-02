package com.debateseason_backend_v1.domain.user.domain.policy;

import java.util.List;

import com.debateseason_backend_v1.domain.user.domain.rules.Rule;

public interface Policy<T> extends Rule<T> {

	List<? extends Rule<? super T>> rules();

	@Override
	default void check(T target) {
		for (var r : rules())
			r.check(target);
	}
	
}