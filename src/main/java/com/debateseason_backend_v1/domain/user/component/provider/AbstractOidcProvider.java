package com.debateseason_backend_v1.domain.user.component.provider;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractOidcProvider implements OidcProvider {

	protected final JwkProvider jwkProvider;
	protected final String issuer;
	protected final String audience;

	/**
	 * 생성자
	 *
	 * @param jwksUrl JWKS 엔드포인트 URL
	 * @param issuer 토큰 발급자
	 * @param audience 토큰 대상자
	 */
	protected AbstractOidcProvider(String jwksUrl, String issuer, String audience) {

		this.issuer = issuer;
		this.audience = audience;

		try {
			// JWK 제공자 초기화 및 캐싱 설정
			this.jwkProvider = new JwkProviderBuilder(new URL(jwksUrl))
				.cached(10, 7, TimeUnit.DAYS) // 최대 10개 키를 7일간 캐싱 (메모리 최적화)
				.rateLimited(10, 1, TimeUnit.MINUTES) // 외부 API 요청 제한 (네트워크 최적화)
				.build();
		} catch (MalformedURLException e) {
			log.error("Invalid JWKS URL: {}", jwksUrl, e);
			throw new CustomException(ErrorCode.INVALID_JWKS_URL);
		}
	}

	/**
	 * ID 토큰에서 사용자 ID 추출
	 * - OidcProvider 인터페이스 구현
	 *
	 * @param idToken 검증할 ID 토큰
	 * @return 사용자 식별자 (sub claim)
	 */
	@Override
	public String extractUserId(String idToken) {

		DecodedJWT verifiedToken = verifyToken(idToken);
		return verifiedToken.getSubject();
	}

	/**
	 * ID 토큰 검증
	 *
	 * @param idToken 검증할 ID 토큰
	 * @return 검증된 토큰 객체
	 */
	protected DecodedJWT verifyToken(String idToken) {

		DecodedJWT decodedToken = decodeToken(idToken);
		Jwk jwk = fetchJwk(decodedToken.getKeyId());
		RSAPublicKey publicKey = extractPublicKey(jwk);
		JWTVerifier verifier = createVerifier(publicKey);

		try {
			return verifier.verify(idToken);
		} catch (JWTVerificationException e) {
			log.error("ID token signature validation failed", e);
			throw new CustomException(ErrorCode.ID_TOKEN_SIGNATURE_VALIDATION_FAILED);
		}
	}

	/**
	 * ID 토큰 디코딩 (서명 검증 없이)
	 *
	 * @param idToken 디코딩할 ID 토큰
	 * @return 디코딩된 토큰 객체
	 */
	protected DecodedJWT decodeToken(String idToken) {

		try {
			return JWT.decode(idToken);
		} catch (JWTDecodeException e) {
			log.error("Failed to decode ID token", e);
			throw new CustomException(ErrorCode.ID_TOKEN_DECODING_FAILED);
		}
	}

	/**
	 * JWK 가져오기 (캐시 또는 원격 엔드포인트에서)
	 *
	 * @param kid 키 ID
	 * @return JWK 객체
	 */
	protected Jwk fetchJwk(String kid) {

		try {
			return jwkProvider.get(kid);
		} catch (JwkException e) {
			log.error("JWKS processing failed for kid: {}", kid, e);
			throw new CustomException(ErrorCode.JWKS_RETRIEVAL_FAILED);
		}
	}

	/**
	 * JWK에서 RSA 공개키 추출
	 *
	 * @param jwk JWK 객체
	 * @return RSA 공개키
	 */
	protected RSAPublicKey extractPublicKey(Jwk jwk) {

		try {
			return (RSAPublicKey)jwk.getPublicKey();
		} catch (InvalidPublicKeyException e) {
			log.error("Invalid public key for JWK with kid: {}", jwk.getId(), e);
			throw new CustomException(ErrorCode.PUBLIC_KEY_EXTRACTION_FAILED);
		}
	}

	/**
	 * JWT 검증기 생성
	 *
	 * @param publicKey RSA 공개키
	 * @return JWT 검증기
	 */
	protected JWTVerifier createVerifier(RSAPublicKey publicKey) {

		return JWT.require(Algorithm.RSA256(publicKey, null))
			.withIssuer(issuer)
			.withAudience(audience)
			.build();
	}
}
