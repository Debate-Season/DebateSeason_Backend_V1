package com.debateseason_backend_v1.security.partner;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.security.service.ServiceApiKeys;

/**
 * 고객사 API 키. 형식·교체 방식은 서비스 키와 같다: {@code PARTNER_API_KEYS=tmoney-go:<키>[,tmoney-go:<새 키>]}.
 * 이름이 고객사 식별자다 — 조회 수·미터링·레이트 리밋이 이 이름 단위로 나뉜다.
 *
 * <p>서비스 키(SERVICE_API_KEYS)와 반드시 분리한다. 섞으면 고객사 키로 관리 API 가 열린다.
 */
@Component
public class PartnerApiKeys {

	private final ServiceApiKeys keys;

	public PartnerApiKeys(@Value("${partner-api.keys:}") String raw) {
		this.keys = new ServiceApiKeys(raw, "고객사 API 키");
	}

	public Optional<String> resolve(String presented) {
		return keys.resolve(presented);
	}
}
