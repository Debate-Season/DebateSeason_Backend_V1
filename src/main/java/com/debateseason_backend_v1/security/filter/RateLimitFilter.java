package com.debateseason_backend_v1.security.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ErrorResponse;
import com.debateseason_backend_v1.security.CustomUserDetails;
import com.debateseason_backend_v1.security.component.SecurityPathMatcher;
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

	private final SecurityPathMatcher securityPathMatcher;
	private final ObjectMapper objectMapper;
	private final long anonymousRateLimit;
	private final long authenticatedRateLimit;

	private final ConcurrentHashMap<String, BucketEntry> buckets = new ConcurrentHashMap<>();
	private final ScheduledExecutorService cleanupExecutor;

	public RateLimitFilter(
		SecurityPathMatcher securityPathMatcher,
		ObjectMapper objectMapper,
		long anonymousRateLimit,
		long authenticatedRateLimit
	) {
		this.securityPathMatcher = securityPathMatcher;
		this.objectMapper = objectMapper;
		this.anonymousRateLimit = anonymousRateLimit;
		this.authenticatedRateLimit = authenticatedRateLimit;

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
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		if (isExcluded(request.getRequestURI())) {
			filterChain.doFilter(request, response);
			return;
		}

		String bucketKey;
		long rateLimit;

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (isAuthenticated(authentication)) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			bucketKey = "user:" + userDetails.getUserId();
			rateLimit = authenticatedRateLimit;
		} else {
			bucketKey = "ip:" + resolveClientIp(request);
			rateLimit = anonymousRateLimit;
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

	private boolean isExcluded(String requestURI) {
		org.springframework.util.AntPathMatcher pathMatcher = new org.springframework.util.AntPathMatcher();
		for (String pattern : EXCLUDED_PATHS) {
			if (pathMatcher.match(pattern, requestURI)) {
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
