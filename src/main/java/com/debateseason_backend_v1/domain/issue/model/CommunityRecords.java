package com.debateseason_backend_v1.domain.issue.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.debateseason_backend_v1.domain.user.dto.CommunityWithIssueId;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityRecords { // 자주 들락날락할텐데 DB에 기록하면 매우느림. 그리고 나중에 사용자 많아지면, OOM발생할거 같은데.

	// Key는 중복되면 안됨. User는 고유하기 때문에 key로 사용
	private static HashMap<Long, CommunityWithIssueId> recodings = new HashMap<>();

	// synchronize 처리해야하나? 근데 user는 고유한데. <- 고민
	public static void record(UserDTO userDTO, Long issueId) {
		// override를 쓰는게, if-else 분기문보다 나은듯.
		CommunityWithIssueId CWI = new CommunityWithIssueId();
		CWI.setIssueId(issueId);
		CWI.setCommunity(userDTO.getCommunity());

		recodings.put(userDTO.getId(), CWI);

	}

	public static Map<String, Integer> getSortedCommunity(Long issueId) {
		Set<Long> users = recodings.keySet();

		Map<String, Integer> map = new HashMap<>();

		for (Long u : users) {
			// 이슈방에 해당하는 user를 찾음.
			if (recodings.get(u).getIssueId() == issueId) {
				// issueId가 1이다 -> 최초면 0 + 1, 아니면, 저장값 + 1
				String community = recodings.get(u).getCommunity();
				map.put(community, map.getOrDefault(community, 0) + 1);
			}
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
