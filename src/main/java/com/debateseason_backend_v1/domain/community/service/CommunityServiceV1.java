package com.debateseason_backend_v1.domain.community.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	public Page<CommunityResponse> getCommunities(Pageable pageable) {

		return communityRepository.findAll(pageable)
			.map(CommunityResponse::from);
	}

	public Page<CommunityResponse> searchByName(String name, Pageable pageable) {
		
		return communityRepository.findByNameContaining(name, pageable)
			.map(CommunityResponse::from);
	}

}
