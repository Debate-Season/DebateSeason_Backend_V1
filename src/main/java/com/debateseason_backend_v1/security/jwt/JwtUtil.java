package com.debateseason_backend_v1.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.common.enums.TokenType;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.user.domain.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

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

	// 검증 실패 로깅은 호출 측(JwtAuthenticationFilter / WebSocketConfig)이 담당한다.
	// 예외 종류에 따라 심각도가 다른데(만료=정상 흐름, 서명 불일치=구 토큰) 여기서는
	// 그 구분을 할 수 없고, 잡았다 다시 던지기만 하면 같은 사건이 두 줄로 남아
	// 에러 알림이 중복 발화한다.
	public boolean validate(String token) {

		extractAllClaims(token);
		return true;
	}

	/**
	 * refresh token 을 검증하고, 실패 사유를 {@link CustomException} 으로 번역한다.
	 *
	 * <p>재발급(reissue)과 로그아웃이 공유한다 -> 두 경로의 판정이 어긋나지 않는다.
	 * 예전에는 재발급이 아무 검증도 하지 않고 로그아웃만 검증해서, 같은 토큰이
	 * 한쪽에서는 통하고 다른 쪽에서는 500 이 나는 상태였다.
	 *
	 * @throws CustomException 만료({@code EXPIRED_REFRESH_TOKEN}) 또는
	 *                         서명 불일치·형식 오류·타입 불일치({@code INVALID_REFRESH_TOKEN})
	 */
	public void validateRefreshToken(String token) {

		try {
			extractAllClaims(token);
		} catch (ExpiredJwtException e) {
			throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
		} catch (JwtException | IllegalArgumentException e) {
			throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		// access token 을 재발급에 쓰지 못하게 한다.
		if (getTokenType(token) != TokenType.REFRESH) {
			throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
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