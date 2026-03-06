package com.debateseason_backend_v1.domain.issue.community;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.user.dto.UserDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class CommunitySorterV4 {

	private final ConcurrentHashMap<Long, List<UserDTO>> usersByIssue = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, Map<Long, Integer>> userIndexByIssue = new ConcurrentHashMap<>();

	// 생성 시, final 객체를 이미 박아 넣어서 @AllargsConstructor 안됨.
	private final CommunityList communityList;
	public CommunitySorterV4(CommunityList communityList) {
		this.communityList = communityList;
	}

	public void record(UserDTO userDTO, Long issueId) {

		// Double checking Lock을 통해서 중복 초기화 문제를 방지.
		if(usersByIssue.get(issueId) != null){

			checkUser(userDTO,issueId);

		}
		else{ // 2. 이슈방 첫 조회시 -> 해당 이슈방 및 userList 생성하기, -> Lock 필수.

			synchronized (this) {

				if (usersByIssue.get(issueId) != null) {

					checkUser(userDTO, issueId);

				}
				else {

					// 1. 위 user가 그 usersByIssue에서 어디에 위치하는지 표시.
					// 이걸 먼저 해야만 동시성 문제가 발생하지 않는다.
					Map<Long, Integer> userIdWithIndex = new HashMap<>();
					userIdWithIndex.put(userDTO.getId(), 0);
					userIndexByIssue.put(issueId, userIdWithIndex);

					// 2. userList 생성 및 최초 이슈방 방문자 등록
					List<UserDTO> newUserList = new ArrayList<>();
					newUserList.add(userDTO);
					usersByIssue.put(issueId, newUserList);

				}
			}

		}
	}

	private void checkUser(UserDTO userDTO, Long issueId){

		// 1. 그 이슈에서 지금 방문한 사용자의 id와 index.
		Map<Long, Integer> userPositionMap = userIndexByIssue.get(issueId); // 단순 조회 용도로만 사용.
		Integer index = userPositionMap.get(userDTO.getId());

		if (index == null) { // 해당 이슈방에 대해서 최초 방문자인 경우.

			synchronized (this){

				// 동시에 접근하더라도, 이 코드를 실행하기 위해서는 한 줄씩 대기.
				List<UserDTO> userList = usersByIssue.get(issueId); //
				userList.add(userDTO);

				index = userList.size() - 1;

				// markIndex_cashUsers에 기록하기
				Map<Long, Integer> map = userIndexByIssue.get(issueId);
				map.put(userDTO.getId(), index);

			}


		} else { // 2회 이상 다중 방문한 사용자인 경우, 사용자마다 고유한 id를 index를 동시에 접근할리가 절대 없다.

			List<UserDTO> userList = usersByIssue.get(issueId);

			UserDTO getUser = userList.get(index);
			getUser.setCommunity(userDTO.getCommunity()); // if문으로 변경감지를 할 바에 차라리 덮어 씌우는 것이 비용적으로 낫다.

		}

	}

	public LinkedHashMap<String, Integer> getSortedCommunity(Long issueId) {

		// 1. 특정 이슈방에 대한 userList 가져오기
		List<UserDTO> userList = usersByIssue.get(issueId);

		if (userList == null) {
			return new LinkedHashMap<>();
		}

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
