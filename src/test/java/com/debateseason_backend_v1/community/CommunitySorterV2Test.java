package com.debateseason_backend_v1.community;

import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import com.debateseason_backend_v1.domain.issue.community.CommunityList;
import com.debateseason_backend_v1.domain.issue.community.CommunitySorterV4;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;

import lombok.extern.slf4j.Slf4j;
/*
class BookCountService {

	private int count;

	public void setCount(int count) {
		this.count = count;
	}

	public int getCount() {
		return this.count;
	}

	public void decreaseCount() {
		this.count -= 1;
	}
}

 */
@Slf4j
public class CommunitySorterV2Test {


	/*
	@RepeatedTest(10)
	void Concurrency() throws InterruptedException {
		// 스레드 풀 생성
		ExecutorService executorService = Executors.newFixedThreadPool(32);

		BookCountService bookCountService = new BookCountService();

		// 기존 카운트 100개로 설정
		bookCountService.setCount(100000);
		int executeCount = 100000;
		CountDownLatch countDownLatch = new CountDownLatch(executeCount);

		for (int i = 0; i < executeCount; i++) {
			executorService.execute(() -> {
				bookCountService.decreaseCount();
				countDownLatch.countDown();
			});
		}

		countDownLatch.await();
		assertThat(bookCountService.getCount()).isEqualTo(0);

	}

	 */

	/*
	@RepeatedTest(10)
	void singleRoom_MultiUsers() throws InterruptedException {

		// 스레드 풀 생성
		ExecutorService executorService = Executors.newFixedThreadPool(20); // 병렬로 실행이 가능
		CountDownLatch countDownLatch = new CountDownLatch(1000000);

		CommunityList communityList = new CommunityList();
		CommunitySorterV4 testSorter = new CommunitySorterV4(communityList);

		String[] communityNames = new String[5];
		communityNames[0]="디시인사이드";
		communityNames[1]="더쿠";
		communityNames[2]="에펨코리아";
		communityNames[3]="뽐뿌";
		communityNames[4]="인벤";

		final Long issueId = 1L;
		AtomicLong userIdGen = new AtomicLong(1);

		// 총 10만 건 실행.
		for(int i=0; i<1000000; i++){

			int taskNum = (i%5);

			executorService.execute(
				()->{
					UserDTO user = new UserDTO();
					user.setId(userIdGen.getAndIncrement()); // userId는 원자적으로 1씩 증가. Race Condtion 때문에.
					user.setCommunity(communityNames[taskNum]);
					testSorter.record(user, issueId);
					countDownLatch.countDown();

				});
		}
		countDownLatch.await();
		executorService.shutdown();

		// 모든 커뮤니티에서 200000이 출력되어야 한다.
		int expected = 200000;

		LinkedHashMap hashMap = testSorter.getSortedCommunity(issueId);


		// UI에 보여줄 이미지가 필요하므로, 커뮤니티 이름이 아닌 주소값을 반환한다.
		assertEquals(expected,hashMap.get("community/icons/inven.png")); // 인벤
		assertEquals(expected,hashMap.get("community/icons/dcinside.png")); // 디시인사이드
		assertEquals(expected,hashMap.get("community/icons/theqoo.png")); // 더쿠
		assertEquals(expected,hashMap.get("community/icons/ppomppu.png")); // 뽐뿌
		assertEquals(expected,hashMap.get("community/icons/fmkorea.png")); // 에펨코리아


	}









	@RepeatedTest(10)
	void singleRoom_MultiUsers_Changing_Community() throws InterruptedException {

		CommunityList communityList = new CommunityList();
		CommunitySorterV4 testSorter = new CommunitySorterV4(communityList);

		CountDownLatch countDownLatch = new CountDownLatch(190000);

		String[] communityNames = new String[7];
		communityNames[0] = "디시인사이드";
		communityNames[1] = "더쿠";
		communityNames[2] = "에펨코리아";
		communityNames[3] = "뽐뿌";
		communityNames[4] = "인벤";
		communityNames[5] = "오르비";
		communityNames[6] = "블라인드";


		final Long issueId = 1L;


		// 1. Main 스레드로 0 ~ 49999 까지는 디시인사이드로 초기화를 한다.
		for(int i=0; i<50000; i++){
			UserDTO userDTO = new UserDTO();
			userDTO.setId((long)i);
			userDTO.setCommunity(communityNames[0]);

			testSorter.record(userDTO,issueId);
			countDownLatch.countDown();

		}

		// 2. 50000 ~ 74999(75000) 까지는 더쿠로 초기화를 한다.
		Thread th2 = new Thread(() -> add(50000,100000,1,communityNames,testSorter,issueId,countDownLatch));

		// 3. 75000 ~ 99999(100000) 까지는 에펨코리아로 초기화를 한다.
		Thread th3 = new Thread(() -> add(100000,150000,2,communityNames,testSorter,issueId,countDownLatch));


		// 수정
		Thread th4 = new Thread(()-> add(0,10000,3,communityNames,testSorter,issueId,countDownLatch));

		Thread th5 = new Thread(()-> add(10000,20000,4,communityNames,testSorter,issueId,countDownLatch));

		Thread th6 = new Thread(()-> add(20000,30000,5,communityNames,testSorter,issueId,countDownLatch));

		Thread th7 = new Thread(()-> add(30000,40000,6,communityNames,testSorter,issueId,countDownLatch));

		th6.start();
		th7.start();

		th2.start(); // 추가
		th4.start();

		th3.start(); // 추가
		th5.start();

		countDownLatch.await(); // main 스레드는 countDown값이 0이 될 때까지, 대기를 한다.

		//pringOut(testSorter.getSortedCommunity(issueId));
		LinkedHashMap hashMap = testSorter.getSortedCommunity(issueId);

		int expected1 = 10000;
		int expected2 = 50000;

		// UI에 보여줄 이미지가 필요하므로, 커뮤니티 이름이 아닌 주소값을 반환한다.
		assertEquals(expected2,hashMap.get("community/icons/fmkorea.png")); // 에펨코리아 -> 50,000
		assertEquals(expected2,hashMap.get("community/icons/theqoo.png")); // 더쿠 -> 50,000
		assertEquals(expected1,hashMap.get("community/icons/inven.png")); // 인벤
		assertEquals(expected1,hashMap.get("community/icons/dcinside.png")); // 디시인사이드
		assertEquals(expected1,hashMap.get("community/icons/ppomppu.png")); // 뽐뿌
		assertEquals(expected1,hashMap.get("community/icons/blind.png")); // 뽐뿌

	}




	@RepeatedTest(10)
	public void MultiRoom_MultiUser() throws InterruptedException {

		// 스레드 풀 생성
		ExecutorService executorService = Executors.newFixedThreadPool(20); // 병렬로 실행이 가능
		CountDownLatch countDownLatch = new CountDownLatch(1000000);

		CommunityList communityList = new CommunityList();
		CommunitySorterV4 testSorter = new CommunitySorterV4(communityList);

		String[] communityNames = new String[5];
		communityNames[0]="디시인사이드";
		communityNames[1]="더쿠";
		communityNames[2]="에펨코리아";
		communityNames[3]="뽐뿌";
		communityNames[4]="인벤";

		AtomicLong userIdGen = new AtomicLong(1);

		// 총 1,000,000 건 실행.
		for(int i=0; i<1000000; i++){

			int taskNum = (i%5);

			executorService.execute(
				()->{

					ThreadLocalRandom rnd = ThreadLocalRandom.current();
					long issueId = rnd.nextInt(1, 4);// 1~3까지

					UserDTO user = new UserDTO();
					user.setId(userIdGen.getAndIncrement()); // userId는 원자적으로 1씩 증가. Race Condtion 때문에.
					user.setCommunity(communityNames[taskNum]);
					testSorter.record(user, issueId);
					countDownLatch.countDown();

				});
		}
		countDownLatch.await();
		executorService.shutdown();

		int totalCount = 0;

		// 각 이슈방마다 등록된 커뮤니티 전체 사용자 수를 반환한다.
		// 만약 동시성 문제가 발생한다면, 데이터 정합성이 깨지기 때문에 1,000,000이 출력될 수 없다.
		totalCount += counting(testSorter.getSortedCommunity(1L));
		totalCount += counting(testSorter.getSortedCommunity(2L));
		totalCount += counting(testSorter.getSortedCommunity(3L));

		assertEquals(1000000,totalCount);


	}

	int counting(LinkedHashMap<String,Integer> hashMap){

		int count = 0;

		for (Object key : hashMap.keySet()) {

			String community = (String)key;
			Integer number = (Integer)hashMap.get(community);

			count = count + number;
		}

		return count;

	}






	@RepeatedTest(10)
	void multiRoom_MultiUsers_Changing_Community() throws InterruptedException {

		CommunityList communityList = new CommunityList();
		CommunitySorterV4 testSorter = new CommunitySorterV4(communityList);

		CountDownLatch countDownLatch = new CountDownLatch(570000);

		String[] communityNames = new String[7];
		communityNames[0] = "디시인사이드";
		communityNames[1] = "더쿠";
		communityNames[2] = "에펨코리아";
		communityNames[3] = "뽐뿌";
		communityNames[4] = "인벤";
		communityNames[5] = "오르비";
		communityNames[6] = "블라인드";

		final Long issueId1 = 1L;
		final Long issueId2 = 2L;
		final Long issueId3 = 3L;

		// 1. 이슈방 1~3번까지 Main 스레드로 0 ~ 49999 까지는 디시인사이드로 초기화를 한다.


			//1번 방 : 0 ~ 49999
			//2번 방 : 0 ~ 49999
			//3번 방 : 0 ~ 49999

		for(int i=1; i<=3; i++){

			for(int j=0; j<50000; j++){
				UserDTO userDTO = new UserDTO();
				userDTO.setId((long)j);
				userDTO.setCommunity(communityNames[0]);

				testSorter.record(userDTO,(long)i);
				countDownLatch.countDown();

			}
		}


		Thread t1 = new Thread(()->{

			// 2. 50000 ~ 74999(75000) 까지는 더쿠로 초기화를 한다.
			Thread th2 = new Thread(() -> add(50000,100000,1,communityNames,testSorter,issueId1,countDownLatch));

			// 3. 75000 ~ 99999(100000) 까지는 에펨코리아로 초기화를 한다.
			Thread th3 = new Thread(() -> add(100000,150000,2,communityNames,testSorter,issueId1,countDownLatch));

			// 수정
			Thread th4 = new Thread(()-> add(0,10000,3,communityNames,testSorter,issueId1,countDownLatch));

			Thread th5 = new Thread(()-> add(10000,20000,4,communityNames,testSorter,issueId1,countDownLatch));

			Thread th6 = new Thread(()-> add(20000,30000,5,communityNames,testSorter,issueId1,countDownLatch));

			Thread th7 = new Thread(()-> add(30000,40000,6,communityNames,testSorter,issueId1,countDownLatch));

			th6.start();
			th7.start();

			th2.start(); // 추가
			th4.start();

			th3.start(); // 추가
			th5.start();

		});

		Thread t2 = new Thread(()->{

			// 2. 50000 ~ 74999(75000) 까지는 더쿠로 초기화를 한다.
			Thread th2 = new Thread(() -> add(50000,100000,1,communityNames,testSorter,issueId2,countDownLatch));

			// 3. 75000 ~ 99999(100000) 까지는 에펨코리아로 초기화를 한다.
			Thread th3 = new Thread(() -> add(100000,150000,2,communityNames,testSorter,issueId2,countDownLatch));

			// 수정
			Thread th4 = new Thread(()-> add(0,10000,3,communityNames,testSorter,issueId2,countDownLatch));

			Thread th5 = new Thread(()-> add(10000,20000,4,communityNames,testSorter,issueId2,countDownLatch));

			Thread th6 = new Thread(()-> add(20000,30000,5,communityNames,testSorter,issueId2,countDownLatch));

			Thread th7 = new Thread(()-> add(30000,40000,6,communityNames,testSorter,issueId2,countDownLatch));

			th6.start();
			th7.start();

			th2.start(); // 추가
			th4.start();

			th3.start(); // 추가
			th5.start();

		});

		Thread t3 = new Thread(()->{

			// 2. 50000 ~ 74999(75000) 까지는 더쿠로 초기화를 한다.
			Thread th2 = new Thread(() -> add(50000,100000,1,communityNames,testSorter,issueId3,countDownLatch));

			// 3. 75000 ~ 99999(100000) 까지는 에펨코리아로 초기화를 한다.
			Thread th3 = new Thread(() -> add(100000,150000,2,communityNames,testSorter,issueId3,countDownLatch));

			// 수정
			Thread th4 = new Thread(()-> add(0,10000,3,communityNames,testSorter,issueId3,countDownLatch));

			Thread th5 = new Thread(()-> add(10000,20000,4,communityNames,testSorter,issueId3,countDownLatch));

			Thread th6 = new Thread(()-> add(20000,30000,5,communityNames,testSorter,issueId3,countDownLatch));

			Thread th7 = new Thread(()-> add(30000,40000,6,communityNames,testSorter,issueId3,countDownLatch));

			th6.start();
			th7.start();

			th2.start(); // 추가
			th4.start();

			th3.start(); // 추가
			th5.start();

		});

		t1.start();
		t2.start();
		t3.start();

		countDownLatch.await(); // countDownLatch가 0이 되어야 다음 스레드(main)가 실행될 수 있다.

		LinkedHashMap hashMap1 = testSorter.getSortedCommunity(issueId1);
		LinkedHashMap hashMap2 = testSorter.getSortedCommunity(issueId2);
		LinkedHashMap hashMap3 = testSorter.getSortedCommunity(issueId3);


		assertEq(hashMap1);
		assertEq(hashMap2);
		assertEq(hashMap3);

	}

	public void assertEq(LinkedHashMap hashMap){

		int expected1 = 10000;
		int expected2 = 50000;

		assertEquals(expected2,hashMap.get("community/icons/fmkorea.png")); // 에펨코리아 -> 50,000
		assertEquals(expected2,hashMap.get("community/icons/theqoo.png")); // 더쿠 -> 50,000
		assertEquals(expected1,hashMap.get("community/icons/inven.png")); // 인벤
		assertEquals(expected1,hashMap.get("community/icons/dcinside.png")); // 디시인사이드
		assertEquals(expected1,hashMap.get("community/icons/ppomppu.png")); // 뽐뿌
		assertEquals(expected1,hashMap.get("community/icons/blind.png")); // 뽐뿌

	}

	/*
	private void add(int start, int end,int index,
		String[] communityNames,CommunitySorterV4 testSorter ,long issueId
		, CountDownLatch countDownLatch){

		for(int i=start; i<end; i++){
			UserDTO userDTO = new UserDTO();
			userDTO.setId((long)i);
			userDTO.setCommunity(communityNames[index]);

			testSorter.record(userDTO,issueId);
			countDownLatch.countDown();

		}

	}
	 */



	/*
	void pringOut(LinkedHashMap<String,Integer> hashMap){

		for (Object key : hashMap.keySet()) {
			String community = (String)key;
			Integer number = hashMap.get(community);

			System.out.println("community: " + community + " number: " + number);
		}

		System.out.println();

	}

		void pringOut(LinkedHashMap<String,Integer> hashMap, Long issueId){

		System.out.println("이슈방 ["+issueId+"]에 대한 집계 결과");

		for (Object key : hashMap.keySet()) {
			String community = (String)key;
			Integer number = hashMap.get(community);

			System.out.println("community: " + community + " number: " + number);
		}

		System.out.println();

	}



	private void add(int start, int end,int index,
		String[] communityNames,CommunitySorterV4 testSorter ,long issueId
		, CountDownLatch countDownLatch){

		for(int i=start; i<end; i++){
			UserDTO userDTO = new UserDTO();
			userDTO.setId((long)i);
			userDTO.setCommunity(communityNames[index]);

			testSorter.record(userDTO,issueId);
			countDownLatch.countDown();

		}

	}

	 */

}
