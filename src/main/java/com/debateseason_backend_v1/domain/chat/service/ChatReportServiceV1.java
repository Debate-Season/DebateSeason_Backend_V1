package com.debateseason_backend_v1.domain.chat.service;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chat.model.request.ChatReportRequest;
import com.debateseason_backend_v1.domain.chat.model.request.ReportProcessRequest;
import com.debateseason_backend_v1.domain.chat.model.response.ChatReportResponse;
import com.debateseason_backend_v1.domain.repository.ChatReactionRepository;
import com.debateseason_backend_v1.domain.repository.ChatReportRepository;
import com.debateseason_backend_v1.domain.repository.ChatRepository;
import com.debateseason_backend_v1.domain.repository.ReportReasonRepository;
import com.debateseason_backend_v1.domain.repository.ChatReportReasonRepository;
import com.debateseason_backend_v1.domain.repository.entity.Chat;
import com.debateseason_backend_v1.domain.repository.entity.ChatReport;
import com.debateseason_backend_v1.domain.repository.entity.ReportReason;
import com.debateseason_backend_v1.domain.repository.entity.ChatReportReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.debateseason_backend_v1.domain.chat.model.response.ChatMessageResponse;
import com.debateseason_backend_v1.common.enums.ReportReasonType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatReportServiceV1 {

    private final ChatRepository chatRepository;
    private final ChatReportRepository chatReportRepository;
    private final ReportReasonRepository reportReasonRepository;
    private final ChatReportReasonRepository chatReportReasonRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatReactionRepository chatReactionRepository;

    @Transactional
    public ApiResult<ChatReportResponse> reportChat(Long chatId, ChatReportRequest request, Long userId) {
        log.info("채팅 신고 처리: chatId={}, reasonTypes={}, userId={}",
                chatId, request.getReasonTypes(), userId);

        if (userId == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_USER, "사용자 정보를 찾을 수 없습니다.");
        }

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "메시지를 찾을 수 없습니다."));

        if (chatReportRepository.existsByChatIdAndReporterId(chatId, userId)) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED, "이미 신고한 메시지입니다.");
        }

        if (chat.getUserId() != null && chat.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.SELF_REPORT_NOT_ALLOWED, "자신의 메시지는 신고할 수 없습니다.");
        }

        String reasonDetail = request.getReasonDetail();
        if (reasonDetail != null) {
            if (reasonDetail.length() > 100) {
                throw new CustomException(ErrorCode.REPORT_REASON_TOO_LONG, "신고 사유는 최대 100자까지 입력 가능합니다.");
            }
        }
        
        // reasonTypes가 null이면 빈 리스트로 초기화
        List<ReportReasonType> reasonTypes = request.getReasonTypes();
        if (reasonTypes == null || reasonTypes.isEmpty()) {
            reasonTypes = new ArrayList<>();
            // 기본값으로 OTHER 추가
            reasonTypes.add(ReportReasonType.OTHER);
            log.warn("신고 사유 목록이 비어있어 기본값(OTHER)을 추가합니다. chatId={}, userId={}", chatId, userId);
        }
        
        // 신고 정보 저장
        ChatReport chatReport = ChatReport.builder()
                .chat(chat)
                .reporterId(userId)
                .reasonDetail(reasonDetail)
                .status(ChatReport.ReportStatus.PENDING)
                .build();
        
        // 신고 사유 추가
        for (ReportReasonType reasonType : reasonTypes) {
            ReportReason reportReason = getOrCreateReportReason(reasonType);
            chatReport.addReportReason(reportReason);
        }
        
        ChatReport savedReport = chatReportRepository.save(chatReport);
        
        // 채팅 메시지 상태 업데이트
        chat.updateReportStatus(Chat.ReportStatus.PENDING);
        chatRepository.save(chat);
        
        // 웹소켓으로 신고 상태 업데이트 알림
        ChatMessageResponse response = ChatMessageResponse.from(chat, userId, chatReactionRepository, chatReportRepository);
        messagingTemplate.convertAndSend("/topic/chat/rooms/" + chat.getChatRoomId().getId() + "/reports", response);
        
        return ApiResult.success("메시지 신고가 접수되었습니다.", ChatReportResponse.from(savedReport));
    }

    @Transactional
    public ApiResult<ChatReportResponse> processReport(Long reportId, ReportProcessRequest request, Long adminId) {
        ChatReport report = chatReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "신고 정보를 찾을 수 없습니다."));
        
        // 신고 상태 업데이트
        report.updateStatus(request.getStatus());
        
        // 채팅 메시지 상태 업데이트
        Chat chat = report.getChat();
        Chat.ReportStatus chatStatus = request.getStatus() == ChatReport.ReportStatus.ACCEPTED 
                ? Chat.ReportStatus.ACCEPTED : Chat.ReportStatus.REJECTED;
        chat.updateReportStatus(chatStatus);
        
        // 웹소켓으로 상태 변경 알림
        ChatMessageResponse response = ChatMessageResponse.from(chat, null, chatReactionRepository, chatReportRepository);
        messagingTemplate.convertAndSend("/topic/chat/rooms/" + chat.getChatRoomId().getId() + "/reports", response);
        
        return ApiResult.success("신고 처리가 완료되었습니다.", ChatReportResponse.from(report));
    }
    
    /**
     * 신고 사유 타입에 해당하는 ReportReason 엔티티를 조회하거나 생성합니다.
     */
    private ReportReason getOrCreateReportReason(ReportReasonType reasonType) {
        Optional<ReportReason> existingReason = reportReasonRepository.findByReasonType(reasonType);
        if (existingReason.isPresent()) {
            return existingReason.get();
        } else {
            ReportReason newReason = ReportReason.from(reasonType);
            return reportReasonRepository.save(newReason);
        }
    }
}
