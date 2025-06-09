package com.debateseason_backend_v1.domain.auth.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.debateseason_backend_v1.domain.auth.service.request.TokenReissueServiceRequest;
import com.debateseason_backend_v1.domain.repository.RefreshTokenRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.RefreshToken;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.user.enums.SocialType;

@SpringBootTest
@ActiveProfiles("test")
public class TokenReissueConcurrencyTest {

	@Autowired
	private AuthServiceV1 authService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Test
	@DisplayName("동일한 리프레시 토큰으로 동시에 재발급 요청 시, 한 번만 성공해야 한다")
	void reissueToken_concurrency_test() throws InterruptedException {
		// given
		User testUser = userRepository.save(User.builder()
			.socialType(SocialType.KAKAO)
			.externalId("concurrency_test_user")
			.build());

		String initialRefreshToken = "initial-refresh-token-for-concurrency-test";
		refreshTokenRepository.save(RefreshToken.builder()
			.user(testUser)
			.token(initialRefreshToken)
			.build());

		int threadCount = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failureCount = new AtomicInteger(0);

		TokenReissueServiceRequest request = TokenReissueServiceRequest.builder()
			.refreshToken(initialRefreshToken)
			.build();

		// when
		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					// 각 스레드에서 authService를 직접 호출
					authService.reissueToken(request);
					successCount.incrementAndGet();
				} catch (Exception e) {
					failureCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executorService.shutdown();

		// then
		assertThat(successCount.get()).isEqualTo(1);
		assertThat(failureCount.get()).isEqualTo(threadCount - 1);
	}
}
