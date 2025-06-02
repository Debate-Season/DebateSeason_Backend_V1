package com.debateseason_backend_v1.domain.youtubeLive.infrastructure;

import java.time.LocalDateTime;

import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLive;

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

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "youtube_live")
public class YoutubeLiveEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "title", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
	private String title;

	@Column(name = "supplier")
	private String supplier;

	@Column(name = "video_id")
	private String videoId;

	@Column(name = "category")
	private String category;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "src")
	private String scr;

	// Jpa에서 반환시 어떠한 처리없이 바로 넘기므로, 도메인 엔티티로 전달해도 괜찮다.
	public YoutubeLive from(YoutubeLiveEntity youtubeLiveEntity){
		return YoutubeLive.builder()
			.id(youtubeLiveEntity.getId())
			.title(youtubeLiveEntity.getTitle())
			.supplier(youtubeLiveEntity.getSupplier())
			.videoId(youtubeLiveEntity.getVideoId())
			.category(youtubeLiveEntity.getCategory())
			.createAt(youtubeLiveEntity.getCreatedAt())
			.src(youtubeLiveEntity.getScr())
			.build()
			;
	}
}
