package com.debateseason_backend_v1.domain.user.controller.request;

import com.debateseason_backend_v1.domain.user.service.request.ProfileRegisterServiceRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileRegisterRequest {

	private String imageUrl;
	private String nickname;
	private String community;
	private String gender;
	private String ageRange;

	public ProfileRegisterServiceRequest toServiceRequest(Long userId) {

		return ProfileRegisterServiceRequest.builder()
			.userId(userId)
			.imageUrl(getImageUrl())
			.nickname(getNickname())
			.community(getCommunity())
			.gender(getGender())
			.ageRange(getAgeRange())
			.build();
	}
}