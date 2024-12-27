package com.debateseason_backend_v1.domain.repository.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "profile_community")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileCommunity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "profile_id", unique = true)
	private Long profileId;

	@Column(name = "community_id")
	private Long communityId;

	@CreatedDate
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Builder
	private ProfileCommunity(Long profileId, Long communityId) {

		this.profileId = profileId;
		this.communityId = communityId;
	}

}