package com.debateseason_backend_v1.domain.user.component.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.user.domain.SocialType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KakaoOidcProvider extends AbstractOidcProvider {

	public KakaoOidcProvider(
		@Value("${social.kakao.audience}") String audience
	) {
		super(SocialType.KAKAO.getJwksUrl(), SocialType.KAKAO.getIssuer(), audience);
	}

}