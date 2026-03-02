package com.debateseason_backend_v1.domain.issue.community;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.user.dto.UserDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class CommunitySorterV3 { // 자주 들락날락할텐데 DB에 기록하면 매우느림. 그리고 나중에 사용자 많아지면, OOM발생할거 같은데.

	private final HashMap<Long, List<UserDTO>> usersByIssue = new HashMap<>();
	private final HashMap<Long, Map<Long, Integer>> userIndexByIssue = new HashMap<>();

	// 생성 시, final 객체를 이미 박아 넣어서 @AllargsConstructor 안됨.
	private final CommunityList communityList;
	public CommunitySorterV3(CommunityList communityList) {
		this.communityList = communityList;
	}

	// synchronized를 통해서 Race condition 문제 해결( Read Only가 아니라서, Thread-safe하지 않음. )
	public synchronized void record(UserDTO userDTO, Long issueId) {

		if(usersByIssue.get(issueId) != null){ // 1. 이미 만들어진 이슈방에 대해서( 이슈방은 이미 1명 이상의 사용자를 가지고 있다. ) <- 이 부분이 꺼림직 함.
			checkUser(userDTO,issueId);
		}
		else{ // 2. 이슈방 첫 조회시 -> 해당 이슈방 및 userList 생성하기, -> Lock 필수.

			Map<Long, Integer> userIdWithIndex = new HashMap<>();
			userIdWithIndex.put(userDTO.getId(), 0);
			userIndexByIssue.put(issueId, userIdWithIndex);

			// 2. userList 생성 및 최초 이슈방 방문자 등록
			List<UserDTO> newUserList = new ArrayList<>();
			newUserList.add(userDTO);
			usersByIssue.put(issueId, newUserList);

		}
	}

	private void checkUser(UserDTO userDTO, Long issueId){
		// 1. 그 이슈에서 지금 방문한 사용자의 id와 index.
		Map<Long, Integer> userPositionMap = userIndexByIssue.get(issueId);
		Integer index = userPositionMap.get(userDTO.getId());


		if (index == null) { // 해당 이슈방에 대해서 최초 방문자인 경우.

			// 동시에 접근하더라도, 이 코드를 실행하기 위해서는 한 줄씩 대기.
			List<UserDTO> userList = usersByIssue.get(issueId);
			userList.add(userDTO);

			index = userList.size() - 1; // 이걸 기록해야지 나중에 퇴장시 세션에서 삭제할 수 있다.

			// markIndex_cashUsers에 기록하기
			Map<Long, Integer> map = userIndexByIssue.get(issueId);
			map.put(userDTO.getId(), index);

		} else { // 2회 이상 다중 방문한 사용자인 경우, 사용자마다 고유한 id를 index를 동시에 접근할리가 절대 없다.

			List<UserDTO> userList = usersByIssue.get(issueId);

			UserDTO getUser = userList.get(index);
			// if문으로 변경감지를 할 바에 차라리 덮어 씌우는 것이 비용적으로 낫다.
			getUser.setCommunity(userDTO.getCommunity());

		}

	}

	public LinkedHashMap<String, Integer> getSortedCommunity(Long issueId) {

		// 1. 특정 이슈방에 대한 userList 가져오기
		List<UserDTO> userList = usersByIssue.get(issueId);

		// 특정 이슈방에 대한 community를 센다. -> { community : number }
		HashMap<String, Integer> map = new HashMap<>();

		for (UserDTO u : userList) {
			String community = u.getCommunity(); // community이름 = key , url = value
			String url = communityList.get(community);
			map.put(url, map.getOrDefault(url, 0) + 1);
		}

		// 커뮤니티를 내림차순으로 정렬 후, 순서 유지
		List<String> keySet = new ArrayList<>(map.keySet());
		keySet.sort((o1, o2) -> map.get(o2).compareTo(map.get(o1)));

		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>(); // 이거를 써야 삽입 순서가 유지된다.
		for (String key : keySet) {
			sortedMap.put(key, map.get(key));
		}

		return sortedMap;

	}


}
