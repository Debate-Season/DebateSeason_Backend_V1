package com.debateseason_backend_v1.domain.profile.service.response;

import com.debateseason_backend_v1.domain.profile.domain.CommunityType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "커뮤니티 조회 응답 DTO", description = "커뮤니티 조회 응답")
public record CommunityResponse(
	@Schema(description = "커뮤니티 ID", example = "1")
	Long id,

	@Schema(description = "커뮤니티 이름", example = "디시인사이드")
	String name,

	@Schema(description = "커뮤니티 아이콘 URL", example = "community/icons/dcinside.png")
	String iconUrl
) {

	public static CommunityResponse from(CommunityType communityType) {

		return new CommunityResponse(
			communityType.getId(),
			communityType.getName(),
			communityType.getIconUrl()
		);
	}

}