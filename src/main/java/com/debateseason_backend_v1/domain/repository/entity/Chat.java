package com.debateseason_backend_v1.domain.repository.entity;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.enums.OpinionType;
import com.debateseason_backend_v1.domain.chat.model.request.ChatMessageRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class Chat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom chatRoomId;

	@Column(name = "user_id")
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "message_type")
	private MessageType messageType;

	@Column(name = "content", length = 500)
	private String content;

	@Column(name = "sender")
	private String sender;

	@Enumerated(EnumType.STRING)
	@Column(name = "opinion_type")
	private OpinionType opinionType;

	@Column(name = "user_community")
	private String userCommunity;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
	@Column(name = "time_stamp")
	private LocalDateTime timeStamp;

	@Column(name = "is_reported", nullable = false)
	private boolean isReported = false;

	@Column(name = "report_status")
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private ReportStatus reportStatus = ReportStatus.NONE;

	public enum ReportStatus {
		NONE,       // 신고되지 않음
		PENDING,    // 신고 접수됨
		ACCEPTED,   // 신고 승인됨
		REJECTED    // 신고 거부됨
	}

	public static Chat from(ChatMessageRequest request, ChatRoom chatRoom, Long userId) {
		return Chat.builder()
				.chatRoomId(chatRoom)
				.userId(userId)
				.messageType(request.getMessageType())
				.content(request.getContent())
				.sender(request.getSender())
				.opinionType(request.getOpinionType())
				.userCommunity(request.getUserCommunity())
				.timeStamp(LocalDateTime.now())
				.build();
	}

	public void updateReportStatus(ReportStatus status) {
		this.reportStatus = status;
		this.isReported = status != ReportStatus.NONE;
	}
}
