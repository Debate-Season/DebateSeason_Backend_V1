package com.debateseason_backend_v1.domain.profile.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.profile.service.response.CommunityResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityServiceV1 {

	private final List<CommunityResponse> cachedCommunities;

	public CommunityServiceV1() {

		this.cachedCommunities = Arrays.stream(CommunityType.values())
			.sorted(Comparator
				.<CommunityType>comparingInt(c -> {
					if ("무소속".equals(c.getName()))
						return 0;

					char firstChar = c.getName().charAt(0);
					if (firstChar >= '가' && firstChar <= '힣')
						return 1;
					if ((firstChar >= 'A' && firstChar <= 'Z') ||
						(firstChar >= 'a' && firstChar <= 'z'))
						return 2;
					return 3;
				})
				.thenComparing(CommunityType::getName))
			.map(CommunityResponse::from)
			.toList();
	}

	public List<CommunityResponse> getCommunities() {

		return cachedCommunities;
	}

	public List<CommunityResponse> searchByName(String name) {

		if (name.isBlank()) {
			return cachedCommunities;
		}

		return Arrays.stream(CommunityType.values())
			.filter(type -> type.getName().contains(name))
			.map(CommunityResponse::from)
			.toList();
	}

}
