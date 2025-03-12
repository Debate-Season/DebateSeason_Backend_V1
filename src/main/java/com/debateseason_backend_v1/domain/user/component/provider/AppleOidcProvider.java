package com.debateseason_backend_v1.domain.user.component.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppleOidcProvider extends AbstractOidcProvider {

	public AppleOidcProvider(
		@Value("${social.apple.jwksUrl}") String jwksUrl,
		@Value("${social.apple.issuer}") String issuer,
		@Value("${social.apple.audience}") String audience
	) {
		super(jwksUrl, issuer, audience);
		log.info("AppleOidcProvider initialized with jwksUrl: {}", jwksUrl);
	}

}