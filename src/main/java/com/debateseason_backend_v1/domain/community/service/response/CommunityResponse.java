package com.debateseason_backend_v1.domain.community.service.response;

import com.debateseason_backend_v1.domain.repository.entity.Community;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "커뮤니티 조회 응답")
public record CommunityResponse(
	@Schema(description = "커뮤니티 ID", example = "1")
	Long id,

	@Schema(description = "커뮤니티 이름", example = "디시인사이드")
	String name,

	@Schema(description = "커뮤니티 아이콘 URL", example = "https://d1aqrs2xenvfsd.cloudfront.net/community/icons/dcinside.png")
	String iconUrl
) {

	public static CommunityResponse from(Community community) {

		return new CommunityResponse(
			community.getId(),
			community.getName(),
			community.getIconUrl()
		);
	}

}