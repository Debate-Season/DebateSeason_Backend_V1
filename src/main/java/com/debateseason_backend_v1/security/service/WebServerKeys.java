package com.debateseason_backend_v1.security.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 웹 서버(Vercel SSR)임을 증명하는 키. 레이트 리밋 버킷을 고르는 데만 쓴다 — 인증·인가에는 전혀 관여하지 않는다.
 *
 * <p>웹 SSR 은 토큰 없이 부르는데, 백엔드에는 방문자 IP 가 아니라 Vercel 서버 IP 몇 개만 보인다.
 * 그래서 모든 방문자·크롤러가 비로그인 IP 버킷(분당 100) 하나를 나눠 쓰다가 429 → 웹 500 이 났다.
 * 이 키가 맞는 GET 은 별도 버킷을 쓴다.
 *
 * <p>{@link ServiceApiKeys}(SERVICE_API_KEYS)와 반드시 분리한다. 같은 묶음에 넣으면
 * 웹 키로 X-Service-Key 관리 API(쓰기)가 열린다.
 *
 * <p>설정 형식: {@code WEB_SSR_KEYS=web:<키>[,web:<새 키>]} (키 32자 이상, 교체 방식은 서비스 키와 같다)
 */
@Component
public class WebServerKeys {

	public static final String HEADER = "X-Toronchul-Web-Key";

	private final ServiceApiKeys keys;

	public WebServerKeys(@Value("${web-ssr.keys:}") String raw) {
		this.keys = new ServiceApiKeys(raw, "웹 SSR 키");
	}

	public Optional<String> resolve(String presented) {
		return keys.resolve(presented);
	}
}
