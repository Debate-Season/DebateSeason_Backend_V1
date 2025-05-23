package com.debateseason_backend_v1.domain.youtubeLive.infrastructure;

import java.time.LocalDateTime;

import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLive;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;

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
	private Integer id;

	private String title;

	private String supplier;

	private String videoId;

	private String category;

	private LocalDateTime createAt;

	// 1. 도메인Dto -> JPA엔티티로( Repository에서 처리할 수 있음. )
	// 공유 메소드이므로, static으로 등록. 객체는 아님.
	public static YoutubeLiveEntity toJpaEntity(YoutubeLiveDto youtubeLiveDto){

		return
			YoutubeLiveEntity.builder()
				.title(youtubeLiveDto.getTitle())
				.supplier(youtubeLiveDto.getSupplier())
				.videoId(youtubeLiveDto.getVideoId())
				.category(youtubeLiveDto.getCategory())
				.createAt(youtubeLiveDto.getCreateAt())
				.build()
			;

	}

	// Jpa에서 반환시 어떠한 처리없이 바로 넘기므로, 도메인 엔티티로 전달해도 괜찮다.
	public YoutubeLive from(YoutubeLiveEntity youtubeLiveEntity){
		return YoutubeLive.builder()
			.id(youtubeLiveEntity.getId())
			.title(youtubeLiveEntity.getTitle())
			.supplier(youtubeLiveEntity.getSupplier())
			.videoId(youtubeLiveEntity.getVideoId())
			.category(youtubeLiveEntity.getCategory())
			.createAt(youtubeLiveEntity.getCreateAt())
			.build()
			;
	}
}
