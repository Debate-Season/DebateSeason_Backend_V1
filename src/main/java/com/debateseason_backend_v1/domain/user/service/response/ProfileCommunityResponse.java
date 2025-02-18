package com.debateseason_backend_v1.domain.user.service.response;

import com.debateseason_backend_v1.domain.repository.entity.Community;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필에 등록된 커뮤니티 응답")
public record ProfileCommunityResponse(
	@Schema(description = "커뮤니티 ID", example = "1")
	Long id,

	@Schema(description = "커뮤니티 이름", example = "디시인사이드")
	String name,

	@Schema(description = "커뮤니티 아이콘 URL", example = "https://debate-season.s3.ap-northeast-2.amazonaws.com/community/dcinside.png")
	String iconUrl
) {

	public static ProfileCommunityResponse from(Community community) {

		return new ProfileCommunityResponse(
			community.getId(),
			community.getName(),
			community.getIconUrl()
		);
	}

}