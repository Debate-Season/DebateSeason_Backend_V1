package com.debateseason_backend_v1.domain.user.service.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileRegisterServiceRequest {

	private Long userId;
	private String imageUrl;
	private String nickname;
	private String community;
	private String gender;
	private String ageRange;

	@Builder
	private ProfileRegisterServiceRequest(Long userId, String imageUrl, String nickname,
		String community, String gender, String ageRange) {

		this.userId = userId;
		this.imageUrl = imageUrl;
		this.nickname = nickname;
		this.community = community;
		this.ageRange = ageRange;
		this.gender = gender;
	}
}
