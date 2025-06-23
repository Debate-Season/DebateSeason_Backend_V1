package com.debateseason_backend_v1.domain.media.infrastructure.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatEntity;
import com.debateseason_backend_v1.domain.chat.presentation.dto.chat.request.ChatMessageRequest;
import com.debateseason_backend_v1.domain.media.domain.Media;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "media")
@Builder
public class MediaEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "title",columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
	private String title;

	@Column(name = "url")
	private String url;

	@Column(name = "src")
	private String src;

	@Column(name = "category")
	private String category;

	@Column(name = "media")
	private String media;

	@Column(name = "type")
	private String type;// news, community, youtube

	@Column(name = "count")
	private int count;// 조회수

	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	// JpaEntity -> 도메인 엔티티로 변경
	// @Service는 완전히 JpaEntity와 분리 -> ORM에 의존적이지 않음.
	public Media toModel(MediaEntity mediaEntity){
		return Media.builder()
			.id(mediaEntity.getId())
			.title(mediaEntity.getTitle())
			.url(mediaEntity.getUrl())
			.src(mediaEntity.getSrc())
			.category(mediaEntity.getSrc())
			.media(mediaEntity.getMedia())
			.type(mediaEntity.getType())
			.count(mediaEntity.getCount())
			.createdAt(mediaEntity.getCreatedAt())
			.build();
	}

}