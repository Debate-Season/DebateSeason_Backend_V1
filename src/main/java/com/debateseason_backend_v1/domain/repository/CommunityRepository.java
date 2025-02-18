package com.debateseason_backend_v1.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.debateseason_backend_v1.domain.repository.entity.Community;

public interface CommunityRepository extends JpaRepository<Community, Long> {

	List<Community> findByNameContaining(String name);

	@Query(value = """
		SELECT c FROM Community c 
		ORDER BY 
		    CASE c.name WHEN '무소속' THEN 0 ELSE 1 END,
		    CASE WHEN function('ASCII', function('LEFT', c.name, 1)) BETWEEN 65 AND 122 
		         THEN 2 ELSE 1 END,
		    c.name ASC
		""")
	List<Community> findAllOrderedWithKoreanFirst();

}
