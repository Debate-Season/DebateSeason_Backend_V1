package com.debateseason_backend_v1.domain.issue.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.debateseason_backend_v1.domain.user.dto.UserDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityRecords { // 자주 들락날락할텐데 DB에 기록하면 매우느림. 그리고 나중에 사용자 많아지면, OOM발생할거 같은데.

	// Key는 중복되면 안됨. User는 고유하기 때문에 key로 사용
	private static HashMap<Long, LinkedList<UserDTO>> markUserWithIssue = new HashMap<>();
	private static HashMap<Long, Map<Long, Integer>> markUserIndexAboutUserWithIssue = new HashMap<>();
	private static Map<String,String> communityUrlList = new HashMap<>();

	//
	private CommunityRecords() {}

	// JVM에 로드될 경우 딱 한번만 실행이 된다.
	// 초기화 블록
	static {
		communityUrlList.put("디시인사이드","community/icons/dcinside.png");
		communityUrlList.put("에펨코리아","community/icons/fmkorea.png");
		communityUrlList.put("더쿠","community/icons/theqoo.png");
		communityUrlList.put("뽐뿌","community/icons/ppomppu.png");
		communityUrlList.put("루리웹","community/icons/ruliweb.png");
		communityUrlList.put("엠팍","community/icons/mlbpark.png");
		communityUrlList.put("인벤","community/icons/inven.png");
		communityUrlList.put("네이트판","community/icons/natepann.png");
		communityUrlList.put("아카라이브","community/icons/arcalive.png");
		communityUrlList.put("클리앙","community/icons/clien.png");
		communityUrlList.put("일간베스트","community/icons/ilbe.png");
		communityUrlList.put("인스티즈","community/icons/instiz.png");
		communityUrlList.put("보배드림","community/icons/bobaedream.png");
		communityUrlList.put("웃긴대학","community/icons/humoruniv.png");
		communityUrlList.put("오르비","community/icons/orbi.png");
		communityUrlList.put("오늘의유머","community/icons/todayhumor.png");
		communityUrlList.put("여성시대","community/icons/womensgeneration.png");
		communityUrlList.put("에브리타임","community/icons/everytime.png");
		communityUrlList.put("블라인드","community/icons/blind.png");
		communityUrlList.put("Reddit","community/icons/reddit.png");
		communityUrlList.put("X","community/icons/x.png");
		communityUrlList.put("Threads","community/icons/threads.png");
		communityUrlList.put("무소속","community/icons/independent.png");
	}

	// synchronized를 통해서 Race condition 문제 해결
	public static synchronized void record(UserDTO userDTO, Long issueId) {

		// 이슈방 첫 조회시 -> 첫 방문자에 의해서 cashUsers와 markIndex(User 위치)에 표시
		// (markIndex를 하지 않은 경우, cashUsers에서 UserDTO를 찾을 수 없음.)
		// 그래서 Index를 통해서 빠르게 찾기 위한 목적임.
		if (markUserWithIssue.get(issueId) == null) {

			// cashUsers에 대한 LinkedList 생성 및 저장
			LinkedList<UserDTO> linkedList = new LinkedList<>();
			linkedList.addFirst(userDTO);

			markUserWithIssue.put(issueId, linkedList);

			// markedIndex_cashUsers에 기록
			// { issueId : { userId : Index } } Index는 user가 저장된 위치, userId를 이용해서 Index를 찾을 수 있음

			Map<Long, Integer> userIdWithIndex = new HashMap<>();
			userIdWithIndex.put(userDTO.getId(), 0);

			markUserIndexAboutUserWithIssue.put(issueId, userIdWithIndex);

		}
		// cashUsers.get(issueId) != null
		else {

			int index;
			// 1.해당 issue에 userIndex가 기록되었는지 확인
			Map<Long, Integer> userIdAndIndex = markUserIndexAboutUserWithIssue.get(issueId);

			// 해당 이슈방에 최초 방문자인 경우
			if (userIdAndIndex.get(userDTO.getId()) == null) {

				LinkedList<UserDTO> linkedList = markUserWithIssue.get(issueId);
				linkedList.addLast(userDTO);

				index = linkedList.size() - 1; // 이걸 기록해야지 나중에 퇴장시 세션에서 삭제할 수 있다.

				// markIndex_cashUsers에 기록하기
				Map<Long, Integer> map = markUserIndexAboutUserWithIssue.get(issueId);
				map.put(userDTO.getId(), index);
			} else {
				// 2회 이상 다중 방문자인 경우
				// community값을 덮어 씌운다. <- 쓸데없는 덮어쓰기로 시간 낭비할 수 있으므로, 이는 나중에 수정하자!
				// { issueId : { userId : Index } }
				index = userIdAndIndex.get(userDTO.getId());

				LinkedList<UserDTO> linkedList = markUserWithIssue.get(issueId);
				UserDTO fetcheUserDTO = linkedList.get(index);

				// 만약 기존 사용자가 커뮤니티 정보를 바꿀경우에 덮어 씌우자.
				if(!fetcheUserDTO.getCommunity().equals(userDTO.getCommunity())){
					fetcheUserDTO.setCommunity(userDTO.getCommunity());
				}

			}
		}

	}

	public static Map<String, Integer> getSortedCommunity(Long issueId) {

		// 특정 이슈방에 대한 LinkedList 가져오기. 왜냐하면 userDTO에 community가 저장되어 있기 때문임. <- 이것도 나중에 최적화를 위해서 수정가능함.
		LinkedList<UserDTO> linkedList = markUserWithIssue.get(issueId);

		// 특정 이슈방에 대한 community를 센다. -> { community : number }
		HashMap<String, Integer> map = new HashMap<>();

		for (UserDTO u : linkedList) {
			String community = u.getCommunity(); // community이름 = key , url = value
			String url = communityUrlList.get(community);
			map.put(url, map.getOrDefault(url, 0) + 1);
		}

		// 내림차순으로 정렬
		List<String> keySet = new ArrayList<>(map.keySet());
		keySet.sort((o1, o2) -> map.get(o2).compareTo(map.get(o1)));

		Map<String, Integer> sortedMap = new LinkedHashMap<>();

		for (String key : keySet) {
			sortedMap.put(key, map.get(key));
		}

		return sortedMap;

	}
}
