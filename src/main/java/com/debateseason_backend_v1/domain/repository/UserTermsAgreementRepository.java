package com.debateseason_backend_v1.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.repository.entity.UserTermsAgreement;
import com.debateseason_backend_v1.domain.terms.dto.UserTermsAgreementDto;

@Repository
public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, Long> {

	@Query("""
			SELECT new com.debateseason_backend_v1.domain.terms.dto.UserTermsAgreementDto(
				t.termsType, MAX(uta.createdAt)
			)
			FROM UserTermsAgreement uta
			JOIN Terms t ON uta.termsId = t.id 
			WHERE uta.userId = :userId AND uta.agreed = true
			GROUP BY t.termsType
		""")
	List<UserTermsAgreementDto> findLatestAgreementDatesByUserId(@Param("userId") Long userId);

	@Query("""
		SELECT uta.termsId FROM UserTermsAgreement uta 
		WHERE uta.userId = :userId AND uta.agreed = true
		""")
	Set<Long> findAgreedTermsIdsByUserId(@Param("userId") Long userId);

	Optional<UserTermsAgreement> findByUserIdAndTermsId(Long userId, Long termsId);
}
