package com.debateseason_backend_v1.crolling.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.debateseason_backend_v1.domain.repository.entity.Media;

@Repository
public interface TmpMediaRepository extends JpaRepository<Media,Long> {

}
