package com.debateseason_backend_v1.domain.repository;

import com.debateseason_backend_v1.domain.repository.entity.ChatReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatReportRepository extends JpaRepository<ChatReport, Long> {

    List<ChatReport> findByChatId(Long chatId);
    Optional<ChatReport> findByChatIdAndReporterId(Long chatId, Long reporterId);
    boolean existsByChatIdAndReporterId(Long chatId, Long reporterId);


}
