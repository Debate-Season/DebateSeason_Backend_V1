package com.debateseason_backend_v1.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.repository.entity.Terms;

@Repository
public interface TermsRepository extends JpaRepository<Terms, Long> {

	@Query("SELECT t FROM Terms t " +
		"WHERE (t.termsType, t.version) IN " +
		"(SELECT t2.termsType, MAX(t2.version) FROM Terms t2 GROUP BY t2.termsType)"
	)
	Optional<List<Terms>> findLatestTermsForAllTypes();
}