package com.debateseason_backend_v1.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.enums.TokenType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {

	private SecretKey secretKey;
	private final long accessTokenExpireTime;
	private final long refreshTokenExpireTime;

	public JwtUtil(
		@Value("${jwt.secret.key}") String secret,
		@Value("${jwt.access-token.expire-time}") long accessTokenExpireTime,
		@Value("${jwt.refresh-token.expire-time}") long refreshTokenExpireTime
	) {

		secretKey = new SecretKeySpec(
			secret.getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS256.key().build().getAlgorithm()
		);
		this.accessTokenExpireTime = accessTokenExpireTime;
		this.refreshTokenExpireTime = refreshTokenExpireTime;
	}

	public Long getUserId(String token) {

		return Long.valueOf(extractAllClaims(token).getSubject());
	}

	public TokenType getTokenType(String token) {

		String type = extractAllClaims(token)
			.get("type", String.class);
		return TokenType.valueOf(type);
	}

	public Boolean isExpired(String token) {

		try {
			return extractAllClaims(token)
				.getExpiration()
				.before(new Date());
		} catch (ExpiredJwtException e) {
			return true;
		}
	}

	public String createAccessToken(Long userId) {
		return createJwt(TokenType.ACCESS, userId, accessTokenExpireTime);
	}

	public String createRefreshToken(Long userId) {
		return createJwt(TokenType.REFRESH, userId, refreshTokenExpireTime);
	}

	public boolean validate(String token) {

		try {
			extractAllClaims(token);
			return true;
		} catch (SecurityException | MalformedJwtException |
				 UnsupportedJwtException | IllegalArgumentException |
				 ExpiredJwtException | SignatureException e) {
			log.error("JWT 토큰 검증 실패: {}", e.getMessage());
			throw e;
		}
	}

	public String createJwt(TokenType tokenType, Long userId, Long expiredMs) {

		Date now = new Date();
		Date expiration = new Date(now.getTime() + expiredMs);

		return Jwts.builder()
			.header().type("JWT")
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