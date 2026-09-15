package com.debateseason_backend_v1.domain.youtubeLive.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.entity.YoutubeLiveEntity;

@Repository
public interface YoutubeLiveJpaRepository extends JpaRepository<YoutubeLiveEntity,Integer> {

	// 1.category가 일치하는 엔티티 하나를 가져옴
	YoutubeLiveEntity findByCategory(String category);

	// 2.category가 일치하는 엔티티를 지움.
	//   라이브가 끝났는데 새 방송이 없을 때 크롤러가 호출한다.
	//   파생 삭제 쿼리는 트랜잭션 없이 호출하면 터지므로 여기서 명시한다.
	//   (호출부인 doCroll 에도 @Transactional 이 있지만 그쪽에 의존하지 않는다)
	@Transactional
	void deleteByCategory(String category);

}
