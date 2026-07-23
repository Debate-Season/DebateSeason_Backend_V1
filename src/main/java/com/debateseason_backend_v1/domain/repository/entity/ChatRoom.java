package com.debateseason_backend_v1.domain.repository.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomStatus;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@ToString(exclude = "issueEntity")
@Table(name = "chat_room")
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_room_id")
	private Long id;

	// 무한 로딩을 발생시킬 수 있으므로, 이것은 toString에서 제외.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "issue_id", nullable = false)
	private IssueEntity issueEntity;

	private String title;
	private String content;

	// v1.3.5 채팅방 스레드 통합. 레거시 행은 NULL = THREAD 로 해석한다.
	// CONTAINER = 이슈당 1개(모바일이 보는 방, 메시지 버킷), THREAD = 강등된 기존 방.
	@Enumerated(EnumType.STRING)
	@Column(name = "room_type")
	private ChatRoomType roomType;

	// THREAD 가 소속된 CONTAINER 방 id. CONTAINER/레거시 행은 NULL.
	@Column(name = "container_room_id")
	private Long containerRoomId;

	// 레거시 행은 NULL(= 시스템/수동 생성). 소유권 판정 시 NULL 이면 ADMIN 만 수정 가능.
	@Column(name = "created_by")
	private Long createdBy;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private ChatRoomStatus status;

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// @Builder 는 필드 초기화식을 무시하므로, 생성 경로마다 status 를 빠뜨릴 수 있다.
	// 엔티티가 스스로 기본값을 보장한다.
	@PrePersist
	private void applyDefaultStatus() {
		if (status == null) {
			status = ChatRoomStatus.OPEN;
		}
	}

}
