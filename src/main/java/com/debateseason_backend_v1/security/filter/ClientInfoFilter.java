package com.debateseason_backend_v1.security.filter;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 클라이언트 종류/버전을 MDC 에 실어 접근 로그에서 식별할 수 있게 한다.
 *
 * 목적
 * - 강제 업데이트(app_version.force_update)가 실제로 먹혔는지 확인할 수단이 없다.
 *   구버전 앱이 어떤 API 를 아직 호출하는지 알아야 구 API 차단 시점을 정할 수 있다.
 * - Presence 의 web/app 분리 집계에도 같은 헤더를 재사용한다.
 *
 * 차단은 하지 않는다. 헤더가 없으면 unknown 으로 남긴다.
 */
public class ClientInfoFilter extends OncePerRequestFilter {

	public static final String CLIENT_TYPE_HEADER = "X-Client-Type";
	public static final String CLIENT_VERSION_HEADER = "X-Client-Version";

	private static final String MDC_CLIENT_TYPE = "clientType";
	private static final String MDC_CLIENT_VERSION = "clientVersion";
	private static final String UNKNOWN = "unknown";

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		try {
			MDC.put(MDC_CLIENT_TYPE, defaultIfBlank(request.getHeader(CLIENT_TYPE_HEADER)));
			MDC.put(MDC_CLIENT_VERSION, defaultIfBlank(request.getHeader(CLIENT_VERSION_HEADER)));
			filterChain.doFilter(request, response);
		} finally {
			// 스레드 풀이 재사용되므로 반드시 정리한다.
			MDC.remove(MDC_CLIENT_TYPE);
			MDC.remove(MDC_CLIENT_VERSION);
		}
	}

	private String defaultIfBlank(String value) {
		return (value == null || value.isBlank()) ? UNKNOWN : value;
	}
}
