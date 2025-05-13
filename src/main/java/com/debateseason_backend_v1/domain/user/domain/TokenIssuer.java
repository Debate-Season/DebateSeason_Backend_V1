package com.debateseason_backend_v1.domain.user.domain;

public interface TokenIssuer {

	TokenPair issueTokenPair(UserId userId);
}
