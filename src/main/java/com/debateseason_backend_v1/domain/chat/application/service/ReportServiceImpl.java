package com.debateseason_backend_v1.domain.chat.application.service;

import com.debateseason_backend_v1.domain.auth.service.AuthServiceV1;
import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;
import com.debateseason_backend_v1.domain.chat.application.repository.ReportRepository;
import com.debateseason_backend_v1.domain.chat.domain.model.chat.Chat;
import com.debateseason_backend_v1.domain.chat.domain.model.chat.ChatMapper;
import com.debateseason_backend_v1.domain.chat.domain.model.report.*;
import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import com.debateseason_backend_v1.domain.chat.infrastructure.report.ReportEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ChatRepository chatRepository;
    private final ReportDomainService reportDomainService;
    private final AuthServiceV1 authService;

    @Override
    public Report createChatReport(Long chatId, Long reporterId, Set<ReportReasonType> reportReasonType, String description) {
        reporterId = fetchReporterIdIfAbsent(reporterId);

        reportDomainService.validateNotAlreadyReport(chatId,reporterId, ReportTargetType.CHAT);

        Chat chat = ChatMapper.toDomain(chatRepository.findById(chatId));
        Report report = chat.reportBy(chatId, reporterId, reportReasonType, description);
        report.markAsProcessing();
        ReportEntity reportEntity = reportRepository.save(ReportMapper.toEntity(report));
        return ReportMapper.toDomain(reportEntity);
    }

    @Override
    public void processReport(Long reportId, Long adminId, ReportStatus newStatus, String comment) {

        ReportEntity findedreportEntity = reportRepository.findById(reportId);
        Report report = ReportMapper.toDomain(findedreportEntity);

        switch (newStatus) {
            case PROCESSING:
                report.markAsProcessing();
                break;

            case ACCEPTED:
                report.accept(adminId, comment);

                // 승인된 신고에 대한 후속 조치
                if ("CHAT".equals(report.getTargetType())) {
                    ChatEntity chatEntity = chatRepository.findById(report.getTargetId());
                    Chat chat = ChatMapper.toDomain(chatEntity);
                    Chat reportedChat = chat.maskReportedMessage(chat);
                    ChatEntity reportedChatEntity = ChatMapper.toEntity(reportedChat);
                    chatRepository.save(reportedChatEntity);
                }
                break;

            case REJECTED:
                report.reject(adminId, comment);
                break;

            default:
                throw new IllegalArgumentException("유효하지 않은 신고 상태입니다");
        }

        ReportEntity reportEntity = ReportMapper.toEntity(report);
        reportRepository.save(reportEntity);
    }

    @Override
    public List<Report> getReportsByStatus(ReportStatus status, int page, int size) {
        return List.of();
    }

    @Override
    public void processReportStatusAsMarkPending(Report report) {

    }

    /**
     * 유효한 reporterId를 가져 옵니다. 입력된 reporterId가 없는(null) 경우 현재 인증된 사용자의 ID를 반환합니다.
     * @param reporterId 입력된 reporterId (null일 수 있음)
     * @return 유효한 reporterId 또는 인증 정보가 없을 경우 null
     */
    private Long fetchReporterIdIfAbsent(Long reporterId) {
        if (reporterId != null) {
            return reporterId;
        }
        return authService.getCurrentUserId();
    }
}
