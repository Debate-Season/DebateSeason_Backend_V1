package com.debateseason_backend_v1.security.jwt;

import com.debateseason_backend_v1.domain.user.domain.UserId;

public interface TokenIssuer {

	Tokens issue(UserId userId);
}
