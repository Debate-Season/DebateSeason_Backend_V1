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

@NoArgsConstructor
@AllArgsConstructor
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
	private MessageType messageType;

	@Column(length = 500)
	private String content;

	private String sender;

	@Enumerated(EnumType.STRING)
	private OpinionType opinionType;

	private String userCommunity;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
	private LocalDateTime timeStamp;

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
}
