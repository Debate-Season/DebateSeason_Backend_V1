package com.debateseason_backend_v1.domain.terms.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.domain.repository.TermsRepository;
import com.debateseason_backend_v1.domain.repository.entity.Terms;
import com.debateseason_backend_v1.domain.terms.service.response.LatestTermsResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermsServiceV1 {

	private final TermsRepository termsRepository;

	public List<LatestTermsResponse> getLatestTerms() {

		List<Terms> latestTermsForAllTypes = termsRepository.findLatestTermsForAllTypes().orElse(List.of());

		return latestTermsForAllTypes.stream()
			.map(LatestTermsResponse::from)
			.toList();
	}

}
