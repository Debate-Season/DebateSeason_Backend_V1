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
	private static HashMap<Long, LinkedList<UserDTO>> cashUsers = new HashMap<>();
	private static HashMap<Long, Map<Long, Integer>> markIndex_cashUsers = new HashMap<>();
	//
	private CommunityRecords() {

	}

	// synchronize 처리해야하나? 근데 user는 고유한데. <- 고민
	public static void record(UserDTO userDTO, Long issueId) {

		// 이슈방 첫 조회시 -> 첫 방문자에 의해서 cashUsers와 markIndex에 표시
		// markIndex를 하지 않은 경우, cashUsers에서 UserDTO를 찾을 수 없음.
		// 그래서 Index를 통해서 빠르게 찾기 위한 목적임.
		if (cashUsers.get(issueId) == null) {

			// cashUsers에 대한 LinkedList 생성 및 저장
			LinkedList<UserDTO> linkedList = new LinkedList<>();
			linkedList.addFirst(userDTO);

			cashUsers.put(issueId, linkedList);

			// markedIndex_cashUsers에 기록
			// { issueId : { userId : Index } } Index는 user가 저장된 위치, userId를 이용해서 Index를 찾을 수 있음

			Map<Long, Integer> map = new HashMap<>();
			map.put(userDTO.getId(), 0);

			markIndex_cashUsers.put(issueId, map);

		}
		// cashUsers.get(issueId) != null
		else {

			int index;
			// 1.markIndex_cashUsers에서 해당 issue에 userIndex가 기록되었는지 확인
			Map<Long, Integer> userIdAndIndex = markIndex_cashUsers.get(issueId);

			// 해당 이슈방에 최초 방문자인 경우
			if (userIdAndIndex.get(userDTO.getId()) == null) {

				LinkedList<UserDTO> linkedList = cashUsers.get(issueId);
				linkedList.addLast(userDTO);

				index = linkedList.size() - 1;

				// markIndex_cashUsers에 기록하기
				Map<Long, Integer> map = markIndex_cashUsers.get(issueId);
				map.put(userDTO.getId(), index);
			} else {
				// 2회 이상 다중 방문자인 경우
				// community값을 덮어 씌운다. <- 쓸데없는 덮어쓰기로 시간 낭비할 수 있으므로, 이는 나중에 수정하자!
				index = userIdAndIndex.get(userDTO.getId());

				LinkedList<UserDTO> linkedList = cashUsers.get(issueId);
				UserDTO fetcheUserDTO = linkedList.get(index);
				fetcheUserDTO.setCommunity(userDTO.getCommunity());
			}
		}

	}

	public static Map<String, Integer> getSortedCommunity(Long issueId) {

		// 특정 이슈방에 대한 LinkedList 가져오기. 왜냐하면 userDTO에 community가 저장되어 있기 때문임. <- 이것도 나중에 최적화를 위해서 수정가능함.
		LinkedList<UserDTO> linkedList = cashUsers.get(issueId);

		// 특정 이슈방에 대한 community를 센다. -> { community : number }
		HashMap<String, Integer> map = new HashMap<>();

		for (UserDTO u : linkedList) {
			String community = u.getCommunity();
			map.put(community, map.getOrDefault(community, 0) + 1);
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
