package com.debateseason_backend_v1.domain.repository;

import com.debateseason_backend_v1.domain.repository.entity.ChatReport;
import com.debateseason_backend_v1.domain.repository.entity.ChatReportReason;
import com.debateseason_backend_v1.domain.repository.entity.ReportReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatReportReasonRepository extends JpaRepository<ChatReportReason, Long> {
    List<ChatReportReason> findByChatReport(ChatReport chatReport);
    List<ChatReportReason> findByChatReportId(Long chatReportId);
    List<ChatReportReason> findByReportReason(ReportReason reportReason);
    void deleteByChatReport(ChatReport chatReport);
} 