package com.debateseason_backend_v1.domain.user.component.provider;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.user.enums.SocialType;

@Component
public class OidcProviderFactory {

	private final Map<SocialType, OidcProvider> authProviderMap;
	private final AppleOidcProvider appleOidcProvider;
	private final KakaoOidcProvider kakaoOidcProvider;

	public OidcProviderFactory(
		AppleOidcProvider appleIdTokenHandler,
		KakaoOidcProvider kakaoIdTokenHandler
	) {
		this.authProviderMap = new EnumMap<>(SocialType.class);
		this.appleOidcProvider = appleIdTokenHandler;
		this.kakaoOidcProvider = kakaoIdTokenHandler;

		initialize();
	}

	private void initialize() {
		authProviderMap.put(SocialType.APPLE, appleOidcProvider);
		authProviderMap.put(SocialType.KAKAO, kakaoOidcProvider);
	}

	public String extractUserId(SocialType socialType, String idToken) {
		return getIdTokenHandler(socialType).extractUserId(idToken);
	}

	private OidcProvider getIdTokenHandler(final SocialType socialType) {
		final OidcProvider oidcProvider = authProviderMap.get(socialType);

		if (oidcProvider == null) {
			throw new RuntimeException("지원하지 않는 소셜");
		}

		return oidcProvider;
	}

}