package com.debateseason_backend_v1.security.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ErrorResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;
import com.debateseason_backend_v1.security.component.SecurityPathMatcher;
import com.debateseason_backend_v1.security.partner.PartnerApiKeyFilter;
import com.debateseason_backend_v1.security.service.WebServerKeys;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

	private static final String[] EXCLUDED_PATHS = {
		"/swagger-ui/**",
		"/actuator/**",
		"/ws-stomp/**"
	};

	private static final long ENTRY_EXPIRATION_MILLIS = TimeUnit.MINUTES.toMillis(10);
	private static final long CLEANUP_INTERVAL_MINUTES = 5;
	private static final long WEB_KEY_REPORT_MINUTES = 5;
	private static final int WEB_KEY_SAMPLE_IPS = 5;

	private final SecurityPathMatcher securityPathMatcher;
	private final ObjectMapper objectMapper;
	private final long anonymousRateLimit;
	private final long authenticatedRateLimit;
	private final WebServerKeys webServerKeys;
	private final long webServerRateLimit;
	private final long partnerRateLimit;

	private final ConcurrentHashMap<String, BucketEntry> buckets = new ConcurrentHashMap<>();
	private final ScheduledExecutorService cleanupExecutor;

	// 웹 SSR 키 사용 집계 ("web:<키 이름>" = 맞은 키, "mismatch" = 헤더는 있는데 틀린 키). 키 값은 남기지 않는다.
	// 백엔드는 429 때만 로그를 남겨서, 운영 웹 키가 백엔드와 일치하는지 볼 방법이 없었다 (2026-10-26).
	private final ConcurrentHashMap<String, LongAdder> webKeyCounts = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Set<String>> webKeyIps = new ConcurrentHashMap<>();

	public RateLimitFilter(
		SecurityPathMatcher securityPathMatcher,
		ObjectMapper objectMapper,
		long anonymousRateLimit,
		long authenticatedRateLimit,
		WebServerKeys webServerKeys,
		long webServerRateLimit,
		long partnerRateLimit
	) {
		this.securityPathMatcher = securityPathMatcher;
		this.objectMapper = objectMapper;
		this.anonymousRateLimit = anonymousRateLimit;
		this.authenticatedRateLimit = authenticatedRateLimit;
		this.webServerKeys = webServerKeys;
		this.webServerRateLimit = webServerRateLimit;
		this.partnerRateLimit = partnerRateLimit;

		this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread thread = new Thread(r, "rate-limit-cleanup");
			thread.setDaemon(true);
			return thread;
		});

		this.cleanupExecutor.scheduleAtFixedRate(
			this::cleanupExpiredEntries,
			CLEANUP_INTERVAL_MINUTES,
			CLEANUP_INTERVAL_MINUTES,
			TimeUnit.MINUTES
		);
		this.cleanupExecutor.scheduleAtFixedRate(
			this::reportWebKeyUsage,
			WEB_KEY_REPORT_MINUTES,
			WEB_KEY_REPORT_MINUTES,
			TimeUnit.MINUTES
		);
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		if (isExcluded(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		String bucketKey;
		long rateLimit;

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object partner = request.getAttribute(PartnerApiKeyFilter.PARTNER_ATTRIBUTE);
		if (partner != null) {
			// 고객사는 서버에서 부르므로 IP 가 몇 개뿐이다. 고객사 이름 단위 버킷 (계약 트래픽: 피크 ~50 rps).
			bucketKey = "partner:" + partner;
			rateLimit = partnerRateLimit;
		} else if (isAuthenticated(authentication)) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			bucketKey = "user:" + userDetails.getUserId();
			rateLimit = authenticatedRateLimit;
		} else {
			// 웹 SSR 은 토큰 없이 Vercel 서버 IP 몇 개에서 온다. IP 버킷을 쓰면 모든 방문자가 하나를 나눠 쓴다.
			// 조회(GET)만 별도 버킷을 준다. 키가 없거나 틀리면 평소처럼 IP 버킷이다.
			String presented = request.getHeader(WebServerKeys.HEADER);
			boolean get = "GET".equalsIgnoreCase(request.getMethod());
			Optional<String> webServer = get ? webServerKeys.resolve(presented) : Optional.empty();
			if (get && presented != null) {
				countWebKey(webServer.map(name -> "web:" + name).orElse("mismatch"), resolveClientIp(request));
			}
			if (webServer.isPresent()) {
				bucketKey = "web:" + webServer.get();
				rateLimit = webServerRateLimit;
			} else {
				bucketKey = "ip:" + resolveClientIp(request);
				rateLimit = anonymousRateLimit;
			}
		}

		BucketEntry entry = buckets.computeIfAbsent(bucketKey, k -> new BucketEntry(createBucket(rateLimit)));
		entry.updateLastAccess();

		ConsumptionProbe probe = entry.getBucket().tryConsumeAndReturnRemaining(1);

		if (!probe.isConsumed()) {
			long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;
			log.warn("Rate limit exceeded for key: {}, retry after: {}s", bucketKey, retryAfterSeconds);

			response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
			writeErrorResponse(response);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean isAuthenticated(Authentication authentication) {
		return authentication != null
			&& authentication.isAuthenticated()
			&& authentication.getPrincipal() instanceof CustomUserDetails;
	}

	private boolean isExcluded(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
			uri = uri.substring(contextPath.length());
		}
		org.springframework.util.AntPathMatcher pathMatcher = new org.springframework.util.AntPathMatcher();
		for (String pattern : EXCLUDED_PATHS) {
			if (pathMatcher.match(pattern, uri)) {
				return true;
			}
		}
		return false;
	}

	private String resolveClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			return xForwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private Bucket createBucket(long capacity) {
		return Bucket.builder()
			.addLimit(limit -> limit.capacity(capacity).refillGreedy(capacity, Duration.ofMinutes(1)))
			.build();
	}

	private void writeErrorResponse(HttpServletResponse response) throws IOException {
		ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.RATE_LIMIT_EXCEEDED);

		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		String jsonResponse = objectMapper.writeValueAsString(errorResponse);
		response.getWriter().write(jsonResponse);
	}

	private void countWebKey(String outcome, String ip) {
		webKeyCounts.computeIfAbsent(outcome, k -> new LongAdder()).increment();
		Set<String> ips = webKeyIps.computeIfAbsent(outcome, k -> ConcurrentHashMap.newKeySet());
		if (ips.size() < WEB_KEY_SAMPLE_IPS) {
			ips.add(ip);
		}
	}

	/**
	 * 지난 구간의 웹 키 집계를 비우고 한 줄로 돌려준다. 요청이 없었으면 null.
	 * 예: {@code web:web=812 [76.76.21.21, ...] mismatch=3 [1.2.3.4]} — mismatch 만 있으면 웹 env 의 키 값이 다르다.
	 */
	String drainWebKeyUsage() {
		Map<String, String> parts = new TreeMap<>();
		for (String outcome : webKeyCounts.keySet()) {
			LongAdder count = webKeyCounts.remove(outcome);
			Set<String> ips = webKeyIps.remove(outcome);
			long n = count == null ? 0 : count.sum();
			if (n > 0) {
				parts.put(outcome, n + " " + new TreeSet<>(ips == null ? Set.of() : ips));
			}
		}
		if (parts.isEmpty()) {
			return null;
		}
		StringBuilder line = new StringBuilder();
		parts.forEach((outcome, value) -> line.append(line.isEmpty() ? "" : " ").append(outcome).append('=').append(value));
		return line.toString();
	}

	private void reportWebKeyUsage() {
		try {
			String line = drainWebKeyUsage();
			if (line != null) {
				log.info("Web SSR key usage (last {}m): {}", WEB_KEY_REPORT_MINUTES, line);
			}
		} catch (RuntimeException e) {
			log.warn("Web SSR key usage report failed", e);
		}
	}

	private void cleanupExpiredEntries() {
		long now = System.currentTimeMillis();
		int removed = 0;

		var iterator = buckets.entrySet().iterator();
		while (iterator.hasNext()) {
			var entry = iterator.next();
			if (now - entry.getValue().getLastAccessTime() > ENTRY_EXPIRATION_MILLIS) {
				iterator.remove();
				removed++;
			}
		}

		if (removed > 0) {
			log.debug("Rate limit cleanup: removed {} expired entries, remaining: {}", removed, buckets.size());
		}
	}

	private static class BucketEntry {
		private final Bucket bucket;
		private volatile long lastAccessTime;

		BucketEntry(Bucket bucket) {
			this.bucket = bucket;
			this.lastAccessTime = System.currentTimeMillis();
		}

		Bucket getBucket() {
			return bucket;
		}

		long getLastAccessTime() {
			return lastAccessTime;
		}

		void updateLastAccess() {
			this.lastAccessTime = System.currentTimeMillis();
		}
	}
}
