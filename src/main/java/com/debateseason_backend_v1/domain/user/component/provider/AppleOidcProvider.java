package com.debateseason_backend_v1.domain.user.component.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.user.domain.OAuthProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppleOidcProvider extends AbstractOidcProvider {

	public AppleOidcProvider(
		@Value("${social.apple.audience}") String audience
	) {
		super(OAuthProvider.APPLE.getJwksUrl(), OAuthProvider.APPLE.getIssuer(), audience);
	}

}