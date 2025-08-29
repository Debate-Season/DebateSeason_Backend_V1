package com.debateseason_backend_v1.domain.repository.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

}
