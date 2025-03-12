package com.debateseason_backend_v1.domain.user.component.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KakaoOidcProvider extends AbstractOidcProvider {

	public KakaoOidcProvider(
		@Value("${social.kakao.jwksUrl}") String jwksUrl,
		@Value("${social.kakao.issuer}") String issuer,
		@Value("${social.kakao.audience}") String audience
	) {
		super(jwksUrl, issuer, audience);
		log.info("KakaoOidcProvider initialized with jwksUrl: {}", jwksUrl);
	}

}