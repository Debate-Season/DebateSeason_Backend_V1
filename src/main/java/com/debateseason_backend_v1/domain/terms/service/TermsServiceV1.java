package com.debateseason_backend_v1.domain.terms.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.repository.TermsRepository;
import com.debateseason_backend_v1.domain.repository.UserTermsAgreementRepository;
import com.debateseason_backend_v1.domain.repository.entity.Terms;
import com.debateseason_backend_v1.domain.repository.entity.UserTermsAgreement;
import com.debateseason_backend_v1.domain.terms.controller.request.TermsAgreementItem;
import com.debateseason_backend_v1.domain.terms.dto.UserTermsAgreementDto;
import com.debateseason_backend_v1.domain.terms.enums.TermsType;
import com.debateseason_backend_v1.domain.terms.service.request.TermsAgreementServiceRequest;
import com.debateseason_backend_v1.domain.terms.service.response.LatestTermsResponse;
import com.debateseason_backend_v1.domain.terms.service.response.UserTermsAgreementResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsServiceV1 {

	private final TermsRepository termsRepository;
	private final UserTermsAgreementRepository userTermsAgreementRepository;

	public List<LatestTermsResponse> getLatestTerms(Long userId) {

		List<Terms> latestTermsForAllTypes = termsRepository.findAllLatestTerms();

		Set<Long> agreedTermsIdsByUserId = userTermsAgreementRepository.findAgreedTermsIdsByUserId(userId);

		return latestTermsForAllTypes.stream()
			.filter(terms -> !agreedTermsIdsByUserId.contains(terms.getId()))
			.map(LatestTermsResponse::from)
			.sorted(Comparator.comparingInt(response -> response.termsType().getDisplayOrder()))
			.toList();
	}

	@Transactional
	public void agree(TermsAgreementServiceRequest request) {

		// 1. 요청 약관 ID 목록 추출
		List<Long> termsIds = extractTermsIds(request);

		// 2. 약관 정보 조회 및 검증
		Map<Long, Terms> termsMap = validateTermsExist(termsIds);

		// 3. 중복 동의 방지 검증
		validateNotDuplicateAgreement(request, termsMap);

		// 4. 필수 약관 동의 여부 검증
		validateRequiredTermsAgreement(request, termsMap);

		// 5. 약관 동의 정보 저장
		saveUserAgreements(request);
	}

	public List<UserTermsAgreementResponse> getUserTermsInfo(Long userId) {

		// 1. 사용자의 약관 동의 정보 조회
		List<UserTermsAgreementDto> userAgreements =
			userTermsAgreementRepository.findLatestAgreementDatesByUserId(userId);

		// 2. 모든 약관 타입에 대한 최신 버전 정보 조회
		List<Terms> latestTerms = termsRepository.findAllLatestTerms();

		// 3. 최신 약관 정보를 맵으로 변환
		Map<TermsType, String> latestUrlMap = latestTerms.stream()
			.collect(Collectors.toMap(
				Terms::getTermsType,
				Terms::getNotionUrl
			));

		return userAgreements.stream()
			.map(agreement -> UserTermsAgreementResponse.of(
				agreement.termsType(),
				agreement.agreedAt(),
				latestUrlMap.get(agreement.termsType())
			))
			.sorted(Comparator.comparingInt(response ->
				response.termsType().getDisplayOrder()
			))
			.toList();
	}

	@Transactional(readOnly = true)
	public boolean hasAgreedToAllRequiredTerms(Long userId) {

		// 1. 최신 필수 약관 ID 조회
		List<Terms> requiredTerms = termsRepository.findAllLatestTerms().stream()
			.filter(Terms::isRequired)
			.toList();

		// 2. 사용자가 동의한 약관 ID 조회
		Set<Long> userAgreedTermsIds = userTermsAgreementRepository
			.findAgreedTermsIdsByUserId(userId);

		return requiredTerms.stream()
			.allMatch(term -> userAgreedTermsIds.contains(term.getId()));
	}

	private List<Long> extractTermsIds(TermsAgreementServiceRequest request) {

		return request.agreements().stream()
			.map(TermsAgreementItem::termsId)
			.distinct()
			.toList();
	}

	private Map<Long, Terms> validateTermsExist(List<Long> termsIds) {

		List<Terms> termsList = termsRepository.findAllById(termsIds);

		Map<Long, Terms> termsMap = termsList.stream()
			.collect(Collectors.toMap(Terms::getId, Function.identity()));

		// 요청의 약관을 찾을수 없으면
		if (termsMap.size() != termsIds.size()) {
			throw new CustomException(ErrorCode.NOT_FOUND_TERMS);
		}

		return termsMap;
	}

	private void validateNotDuplicateAgreement(
		TermsAgreementServiceRequest request,
		Map<Long, Terms> termsMap
	) {
		for (TermsAgreementItem item : request.agreements()) {
			Optional<UserTermsAgreement> existingAgreement =
				userTermsAgreementRepository.findByUserIdAndTermsId(
					request.userId(),
					item.termsId()
				);

			// 이미 동의한 약관에 대해 중복 동의 시 예외 발생
			if (existingAgreement.isPresent()) {
				throw new CustomException(ErrorCode.ALREADY_AGREED_TERMS);
			}
		}
	}

	private void validateRequiredTermsAgreement(TermsAgreementServiceRequest request, Map<Long, Terms> termsMap) {

		// 1. 필수 약관 목록 조회
		Set<Long> requiredTermsIds = termsMap.values().stream()
			.filter(terms -> terms.getTermsType().isRequired())
			.map(Terms::getId)
			.collect(Collectors.toSet());

		// 2. 사용자가 동의한 약관 ID 목록 추출
		Set<Long> agreedTermsIds = request.agreements().stream()
			.filter(TermsAgreementItem::agreed)
			.map(TermsAgreementItem::termsId)
			.collect(Collectors.toSet());

		// 3. 필수 약관 중 동의하지 않은 약관이 있는지 확인
		if (!agreedTermsIds.containsAll(requiredTermsIds)) {
			throw new CustomException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
		}
	}

	private void saveUserAgreements(TermsAgreementServiceRequest request) {

		List<UserTermsAgreement> agreements = request.agreements().stream()
			.map(item -> UserTermsAgreement.create(
				request.userId(),
				item.termsId(),
				item.agreed()
			))
			.toList();

		userTermsAgreementRepository.saveAll(agreements);  // 일괄 저장으로 변경
	}

}