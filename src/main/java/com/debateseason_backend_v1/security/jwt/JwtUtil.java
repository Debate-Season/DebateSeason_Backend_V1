package com.debateseason_backend_v1.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.enums.TokenType;
import com.debateseason_backend_v1.domain.user.domain.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {

	private static final String ROLE_CLAIM = "role";

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

	// 구버전 access token 에는 role 클레임이 없다 -> USER 로 간주 (access token 만료 주기 내에 자연 수렴)
	public UserRole getRole(String token) {

		String role = extractAllClaims(token)
			.get(ROLE_CLAIM, String.class);
		return UserRole.fromNullable(role);
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

	public String createAccessToken(Long userId, UserRole role) {
		return createJwt(TokenType.ACCESS, userId, accessTokenExpireTime, role);
	}

	public String createRefreshToken(Long userId) {
		// refresh token 은 재발급에만 쓰이고 인가에 쓰이지 않으므로 role 을 싣지 않는다.
		return createJwt(TokenType.REFRESH, userId, refreshTokenExpireTime, null);
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

	public String createJwt(TokenType tokenType, Long userId, Long expiredMs, UserRole role) {

		Date now = new Date();
		Date expiration = new Date(now.getTime() + expiredMs);

		JwtBuilder builder = Jwts.builder()
			.header().type("JWT")
			.and()
			.subject(userId.toString())
			.issuedAt(now)
			.expiration(expiration)
			.claim("type", tokenType.name());

		if (role != null) {
			builder.claim(ROLE_CLAIM, role.name());
		}

		return builder
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