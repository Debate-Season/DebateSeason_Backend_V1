package com.debateseason_backend_v1.domain.community.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.community.service.response.CommunityResponse;
import com.debateseason_backend_v1.domain.repository.CommunityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityServiceV1 {

	private final CommunityRepository communityRepository;

	public List<CommunityResponse> searchByName(String name) {

		if (name.isBlank()) {
			return getCommunities();
		}

		return communityRepository.findByNameContaining(name)
			.stream()
			.map(CommunityResponse::from)
			.toList();
	}

	public List<CommunityResponse> getCommunities() {

		return communityRepository.findAllOrderedWithKoreanFirst()
			.stream()
			.map(CommunityResponse::from)
			.toList();
	}

}
