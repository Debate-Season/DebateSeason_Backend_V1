package com.debateseason_backend_v1.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.enums.TokenType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtUtil {

	private static final long ACCESS_TOKEN_EXPIRE_TIME = 600000L;    // 10분
	private static final long REFRESH_TOKEN_EXPIRE_TIME = 86400000L; // 24시간
	private SecretKey secretKey;

	public JwtUtil(@Value("${spring.jwt.secret}") String secret) {

		secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS256.key().build().getAlgorithm());
	}

	public Long getUserId(String token) {

		String subject = extractAllClaims(token).getSubject();
		return Long.valueOf(subject);
	}

	public TokenType getTokenType(String token) {

		String type = extractAllClaims(token)
			.get("type", String.class);
		return TokenType.valueOf(type);
	}

	public Boolean isExpired(String token) {

		return extractAllClaims(token)
			.getExpiration()
			.before(new Date());
	}

	public String createAccessToken(Long userId) {
		return createJwt(TokenType.ACCESS, userId, ACCESS_TOKEN_EXPIRE_TIME);
	}

	public String createRefreshToken(Long userId) {
		return createJwt(TokenType.REFRESH, userId, REFRESH_TOKEN_EXPIRE_TIME);
	}

	public String createJwt(TokenType tokenType, Long userId, Long expiredMs) {

		Date now = new Date();
		Date expiration = new Date(now.getTime() + expiredMs);

		return Jwts.builder()
			.header()
			.type("JWT")
			.and()
			.subject(userId.toString())
			.issuedAt(now)
			.expiration(expiration)
			.claim("type", tokenType.name())
			.signWith(secretKey)
			.compact();
	}

	private Claims extractAllClaims(String token) {

		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

}