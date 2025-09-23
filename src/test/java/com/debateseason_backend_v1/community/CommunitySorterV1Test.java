package com.debateseason_backend_v1.community;


import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import com.debateseason_backend_v1.domain.issue.community.CommunityList;

import com.debateseason_backend_v1.domain.issue.community.CommunitySorterV3;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;

@ExtendWith(MockitoExtension.class)
public class CommunitySorterV1Test {
	/*

	// 차체 알고리즘 동시성 테스트.
	static List<String> community = new ArrayList<>();

	static {
		community.add("디시인사이드");
		community.add("에펨코리아");
		community.add("더쿠");
		community.add("뽐뿌");
		community.add("루리웹");
		community.add("엠팍");
		community.add("인벤");
		community.add("네이트판");
		community.add("아카라이브");
		community.add("클리앙");
		community.add("일간베스트");
		community.add("인스티즈");
		community.add("보배드림");
		community.add("웃긴대학");
		community.add("오르비");
		community.add("오늘의유머");
		community.add("여성시대");
		community.add("에브리타임");
		community.add("블라인드");
		community.add("Reddit");
		community.add("X");
		community.add("Threads");
		community.add("무소속");
	}

	@Test
	void doubleCheckLock() throws InterruptedException {
		// 시나리오
		// 500개의 스레드가 각 100000건의 USER 생성 후, record 호출 -> 5,000,000건의 요청이 발생.

		final int THREADS = 500;
		final int USERS_PER_THREAD = 10000;
		final long issueId = 1L;

		CommunityList communityList = new CommunityList();
		CommunitySorterV3 newRecord = new CommunitySorterV3(communityList);

		CountDownLatch start = new CountDownLatch(1);     // 동시에 출발
		CountDownLatch done  = new CountDownLatch(THREADS); // 완료 대기
		AtomicLong userIdGen = new AtomicLong(1);

		Thread[] workers = new Thread[THREADS];

		for (int t = 0; t < THREADS; t++) {
			final String communityName = (t < THREADS / 2) ? "디시인사이드" : "더쿠"; // ← 절반 분할

			workers[t] = new Thread(() -> {
				try {
					start.await();
					for (int i = 0; i < USERS_PER_THREAD; i++) {
						UserDTO user = new UserDTO();
						user.setId(userIdGen.getAndIncrement()); // userId는 원자적으로 1씩 증가.
						user.setCommunity(communityName);
						newRecord.record(user, issueId);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					done.countDown();
				}
			}, "worker-" + t);
			workers[t].start();
		}

		long t0 = System.nanoTime();
		start.countDown();  // 모든 스레드 동시에 출발
		done.await();       // 전 스레드 종료 대기
		long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
		System.out.println("concurrency test done; elapsed = " + elapsedMs + " ms");

		// 결과 출력 (기존 코드 재사용)
		LinkedHashMap<String, Integer> sortedMap = newRecord.getSortedCommunity(issueId);
		for (Map.Entry<String, Integer> e : sortedMap.entrySet()) {
			System.out.println(e.getKey() + " → " + e.getValue());
		}

	}

	@Test
	void uniqueIds_twoCommunities_counts_and_p95() throws Exception {

		CommunityList communityList = new CommunityList();
		CommunitySorterV3 newRecord = new CommunitySorterV3(communityList);

		final int THREADS = 500;
		final int USERS_PER_THREAD = 10000;      // 총 5,000,000 ops
		final long issueId = 1001L;            // 테스트 간 간섭 방지: 매번 다른 issueId 사용

		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done  = new CountDownLatch(THREADS);
		AtomicLong userIdGen = new AtomicLong(1);

		Thread[] workers = new Thread[THREADS];
		final int totalOps = THREADS * USERS_PER_THREAD;
		final long[] latencies = new long[totalOps];

		for (int t = 0; t < THREADS; t++) {
			final int base = t * USERS_PER_THREAD;
			final String communityName = (t < THREADS / 2) ? "디시인사이드" : "더쿠";

			workers[t] = new Thread(() -> {
				try {
					start.await();
					for (int i = 0; i < USERS_PER_THREAD; i++) {
						UserDTO user = new UserDTO();
						user.setId(userIdGen.getAndIncrement()); // 전역 유일 ID
						user.setCommunity(communityName);

						long s = System.nanoTime();
						newRecord.record(user, issueId);
						long e = System.nanoTime();
						latencies[base + i] = e - s;
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					done.countDown();
				}
			}, "worker-" + t);
			workers[t].start();
		}

		long t0 = System.nanoTime();
		start.countDown();
		assertTrue(done.await(60, TimeUnit.SECONDS), "timed out (deadlock or stall?)");
		long elapsedNs = System.nanoTime() - t0;
		long elapsedMs = TimeUnit.NANOSECONDS.toMillis(elapsedNs);

		// 결과 수집 및 검증
		LinkedHashMap<String, Integer> sortedMap = newRecord.getSortedCommunity(issueId);

		int dc = sortedMap.getOrDefault("community/icons/dcinside.png", 0);
		int theqoo = sortedMap.getOrDefault("community/icons/theqoo.png", 0);
		int sum = dc + theqoo;

		assertEquals((THREADS / 2) * USERS_PER_THREAD, dc, "디시인사이드 카운트 불일치");
		assertEquals((THREADS - THREADS / 2) * USERS_PER_THREAD, theqoo, "더쿠 카운트 불일치");
		assertEquals(totalOps, sum, "총합 불일치");

		// 지연 분포(p50/p95) + QPS
		Arrays.sort(latencies);
		long p50ns = latencies[(int) (totalOps * 0.50)];
		long p95ns = latencies[(int) (totalOps * 0.95)];
		double seconds = elapsedNs / 1_000_000_000.0;
		double qps = totalOps / seconds;

		System.out.printf(
			Locale.ROOT,
			"OK: elapsed=%d ms, QPS=%.0f, p50=%.3f ms, p95=%.3f ms%n",
			elapsedMs, qps, p50ns / 1e6, p95ns / 1e6
		);

		// 사람이 보기 좋게도 출력(선택)
		sortedMap.forEach((k, v) -> System.out.println(k + " → " + v));
	}

	@Test
	void multiIssue_init_race_and_counts() throws Exception {

		CommunityList communityList = new CommunityList();
		CommunitySorterV3 newRecord = new CommunitySorterV3(communityList);

		final int THREADS = 500;
		final int OPS_PER_THREAD = 10000;  // 총 5,000,000 ops
		final int ISSUE_SPACE = 200;      // 1..200 사이에서 무작위 선택
		final long issueBase = 0L;     // 다른 테스트와 구분

		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done  = new CountDownLatch(THREADS);
		AtomicLong userIdGen = new AtomicLong(1);

		for (int t = 0; t < THREADS; t++) {
			new Thread(() -> {
				ThreadLocalRandom rnd = ThreadLocalRandom.current();
				try {
					start.await();
					for (int i = 0; i < OPS_PER_THREAD; i++) {
						long issueId = issueBase + rnd.nextInt(1, ISSUE_SPACE + 1);
						UserDTO user = new UserDTO();
						user.setId(userIdGen.getAndIncrement());       // 여전히 전역 유일
						user.setCommunity(rnd.nextBoolean() ? "디시인사이드" : "더쿠");
						newRecord.record(user, issueId);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					done.countDown();
				}
			}, "mi-" + t).start();
		}

		start.countDown();
		assertTrue(done.await(60, TimeUnit.SECONDS), "timed out");

		// 모든 이슈 합산이 총 호출 수와 동일해야 함
		long total = 0;
		for (int i = 1; i <= ISSUE_SPACE; i++) {
			long issueId = issueBase + i;
			Map<String, Integer> m = newRecord.getSortedCommunity(issueId);
			if (m != null) total += m.values().stream().mapToInt(Integer::intValue).sum();
		}
		assertEquals((long) THREADS * OPS_PER_THREAD, total, "전체 합계 불일치");
	}

	@Test
	void sameUserId_manyTimes_should_count_as_one() throws Exception {

		CommunityList communityList = new CommunityList();
		CommunitySorterV3 newRecord = new CommunitySorterV3(communityList);

		final int THREADS = 200;
		final int OPS_PER_THREAD = 50000;//10,000,000
		final long issueId = 3001L;
		final long sameUserId = 777L;

		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done  = new CountDownLatch(THREADS);

		for (int t = 0; t < THREADS; t++) {
			final String community = (t % 2 == 0) ? "디시인사이드" : "더쿠";
			new Thread(() -> {
				try {
					start.await();
					for (int i = 0; i < OPS_PER_THREAD; i++) {
						UserDTO u = new UserDTO();
						u.setId(sameUserId);
						u.setCommunity(community);
						newRecord.record(u, issueId);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					done.countDown();
				}
			}).start();
		}

		start.countDown();
		assertTrue(done.await(30, TimeUnit.SECONDS), "timed out");

		Map<String, Integer> m = newRecord.getSortedCommunity(issueId);
		int sum = m.values().stream().mapToInt(Integer::intValue).sum();
		assertEquals(1, sum, "동일 ID 다중 기록이 1을 초과함(중복 삽입 발생)");
		// 두 커뮤니티 중 한쪽만 1이어야 함(최종 승자만 남음)
		int dc = m.getOrDefault("community/icons/dcinside.png", 0);
		int theqoo = m.getOrDefault("community/icons/theqoo.png", 0);
		assertTrue((dc == 1 && theqoo == 0) || (dc == 0 && theqoo == 1),
			"최종 소속이 한 쪽으로 수렴하지 않음");
	}

	 */

}
