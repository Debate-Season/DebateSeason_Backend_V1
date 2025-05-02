package com.debateseason_backend_v1.security.jwt;

import com.debateseason_backend_v1.domain.user.domain.RefreshToken;

public record Tokens(String accessToken, RefreshToken refreshToken) {
}
