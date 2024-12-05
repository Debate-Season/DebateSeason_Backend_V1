package com.debateseason_backend_v1.domain.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Chat { // <- 향후 수정가능성이 있음 Chat을 어떻게 저장하느냐에 따라 필드가 달라질 듯

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne
	private ChatRoom chatRoom;

	// 발신자
	private String sender;
	// 소속 커뮤니티
	private String category;
	private String content;
}
