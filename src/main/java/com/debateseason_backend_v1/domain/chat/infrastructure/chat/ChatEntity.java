package com.debateseason_backend_v1.domain.chat.infrastructure.chat;

import com.debateseason_backend_v1.common.enums.MessageType;
import com.debateseason_backend_v1.common.enums.OpinionType;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity(name = "chat")
public class ChatEntity {

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


	//TODO: 챗 맵퍼 클래스를 만들었으나 삭제하지 못함,  ChatServiceV1 (트랜잭션 스크립트) 에서 사용중으로 나중에 리팩토링 예정
	public static ChatEntity from(ChatMessageRequest request, ChatRoom chatRoom, Long userId) {
		return ChatEntity.builder()
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
