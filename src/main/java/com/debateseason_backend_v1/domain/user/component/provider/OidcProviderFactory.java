package com.debateseason_backend_v1.domain.user.component.provider;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.user.domain.OAuthProvider;

@Component
public class OidcProviderFactory {

	private final Map<OAuthProvider, OidcProvider> authProviderMap;
	private final AppleOidcProvider appleOidcProvider;
	private final KakaoOidcProvider kakaoOidcProvider;

	public OidcProviderFactory(
		AppleOidcProvider appleIdTokenHandler,
		KakaoOidcProvider kakaoIdTokenHandler
	) {
		this.authProviderMap = new EnumMap<>(OAuthProvider.class);
		this.appleOidcProvider = appleIdTokenHandler;
		this.kakaoOidcProvider = kakaoIdTokenHandler;

		initialize();
	}

	private void initialize() {
		authProviderMap.put(OAuthProvider.APPLE, appleOidcProvider);
		authProviderMap.put(OAuthProvider.KAKAO, kakaoOidcProvider);
	}

	public String extractUserId(OAuthProvider OAuthProvider, String idToken) {

		DecodedJWT decodedToken;
		try {
			decodedToken = JWT.decode(idToken);
		} catch (JWTDecodeException e) {
			throw new CustomException(ErrorCode.ID_TOKEN_DECODING_FAILED);
		}

		String tokenIssuer = decodedToken.getIssuer();

		// 요청된 소셜 타입의 발급자와 토큰의 발급자 비교
		if (!OAuthProvider.getIssuer().equals(tokenIssuer)) {
			throw new CustomException(ErrorCode.SOCIAL_TYPE_MISMATCH);
		}

		return getIdTokenHandler(OAuthProvider).extractUserId(idToken);
	}

	private OidcProvider getIdTokenHandler(final OAuthProvider OAuthProvider) {
		final OidcProvider oidcProvider = authProviderMap.get(OAuthProvider);

		if (oidcProvider == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_USER);
		}

		return oidcProvider;
	}

}