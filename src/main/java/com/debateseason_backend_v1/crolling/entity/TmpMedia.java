package com.debateseason_backend_v1.crolling.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
public class TmpMedia {
	/*

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

	 */
}
