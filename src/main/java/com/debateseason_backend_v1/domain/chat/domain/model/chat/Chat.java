package com.debateseason_backend_v1.domain.chat.domain.model.chat;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.enums.OpinionType;
import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.domain.chat.domain.model.report.*;
import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
public class Chat implements ReportTarget {

    private final Long id;

    private final ChatRoom chatRoomId;

    private final Long userId;

    private final MessageType messageType;

    private final String content;

    private final String sender;

    private final OpinionType opinionType;

    private final String userCommunity;

    private final LocalDateTime timeStamp;

    private final String REPORTED_MESSAGE_CONTENT = "신고된 메시지 입니다.";
    @Override
    public Report reportBy(Long chatId, Long reporterId, Set<ReportReasonType> reportReasonTypes, String description) {
        guardSelfReport(reporterId);
        return Report.create(chatId, reporterId, ReportTargetType.CHAT, reportReasonTypes, description);
    }

    @Override
    public List<Report> reports() {
        return List.of();
    }

    @Override
    public ReportStatus reportStatus() {
        return null;
    }

    public void guardSelfReport(Long reporterId) {
        if (this.userId != null && this.userId.equals(reporterId))
            throw new CustomException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
    }

    public Chat maskReportedMessage(Chat chat) {
        return Chat.builder()
                .id(chat.id)
                .chatRoomId(chat.chatRoomId)
                .userId(chat.userId)
                .messageType(chat.messageType)
                .content(chat.REPORTED_MESSAGE_CONTENT)
                .sender(sender)
                .opinionType(opinionType)
                .userCommunity(userCommunity)
                .timeStamp(timeStamp)
                .build();
    }

    @Builder
    private Chat(Long id, ChatRoom chatRoomId, Long userId, MessageType messageType, String content, String sender, OpinionType opinionType,String userCommunity, LocalDateTime timeStamp) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.messageType = messageType;
        this.content = content;
        this.sender = sender;
        this.opinionType = opinionType;
        this.userCommunity = userCommunity;
        this.timeStamp = timeStamp;
    }

}
