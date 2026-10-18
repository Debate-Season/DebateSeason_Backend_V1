package com.debateseason_backend_v1.security.partner;

import java.io.IOException;
import java.util.Optional;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.debateseason_backend_v1.security.component.SecurityPathMatcher;
import com.debateseason_backend_v1.security.error.JwtAuthenticationErrorHandler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * /api/partner/** 는 고객사 키({@code X-Partner-Key})로만 들어온다. 키가 없거나 틀리면 401.
 * 맞으면 고객사 이름을 요청 속성에 싣는다 — 뒤의 RateLimitFilter 와 컨트롤러가 이 이름으로 버킷·조회 수·미터링을 나눈다.
 */
@RequiredArgsConstructor
public class PartnerApiKeyFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Partner-Key";
	public static final String PARTNER_ATTRIBUTE = "toronchul.partner";
	public static final String PARTNER_PATHS = "/api/partner/**";

	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

	private final PartnerApiKeys partnerApiKeys;
	private final SecurityPathMatcher securityPathMatcher;
	private final JwtAuthenticationErrorHandler errorHandler;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		if (!PATH_MATCHER.match(PARTNER_PATHS, securityPathMatcher.resolvePath(request))) {
			filterChain.doFilter(request, response);
			return;
		}

		Optional<String> partner = partnerApiKeys.resolve(request.getHeader(HEADER));
		if (partner.isEmpty()) {
			errorHandler.handleInvalidPartnerKey(response, request.getRequestURI());
			return;
		}
		request.setAttribute(PARTNER_ATTRIBUTE, partner.get());
		filterChain.doFilter(request, response);
	}
}
