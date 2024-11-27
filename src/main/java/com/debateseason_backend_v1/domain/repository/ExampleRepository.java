package com.debateseason_backend_v1.domain.repository;

import io.swagger.v3.oas.models.examples.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExampleRepository extends JpaRepository<Example, Long> {

    //패키지 구조 이해를 위한 예시이며 삭제 하셔도 됩니다. - ksb

}
