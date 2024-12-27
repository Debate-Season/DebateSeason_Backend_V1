package com.debateseason_backend_v1.domain.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "community")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Community {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "community_id")
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "image_url")
	private String imageUrl;

}