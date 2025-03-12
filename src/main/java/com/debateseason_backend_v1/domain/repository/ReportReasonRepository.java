package com.debateseason_backend_v1.domain.repository;

import com.debateseason_backend_v1.common.enums.ReportReasonType;
import com.debateseason_backend_v1.domain.repository.entity.ReportReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportReasonRepository extends JpaRepository<ReportReason, Long> {
    Optional<ReportReason> findByReasonType(ReportReasonType reasonType);
} 