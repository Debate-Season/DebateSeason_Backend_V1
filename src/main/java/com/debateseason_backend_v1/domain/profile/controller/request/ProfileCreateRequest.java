package com.debateseason_backend_v1.domain.profile.controller.request;

import com.debateseason_backend_v1.domain.profile.domain.CommunityId;
import com.debateseason_backend_v1.domain.profile.domain.Nickname;
import com.debateseason_backend_v1.domain.profile.domain.PersonalInfo;
import com.debateseason_backend_v1.domain.profile.enums.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.enums.GenderType;
import com.debateseason_backend_v1.domain.profile.service.request.ProfileCreateServiceRequest;
import com.debateseason_backend_v1.domain.user.domain.UserId;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(title = "프로필 등록 요청 DTO", description = "프로필 등록 요청")
public record ProfileCreateRequest(
	@Schema(description = "프로필 컬러", example = "RED")
	String profileImage,

	@Schema(description = "사용자 닉네임", example = "토론왕")
	@NotBlank(message = "닉네임은 필수입니다.")
	String nickname,

	@Schema(description = "소속 커뮤니티 ID", example = "1")
	@NotNull(message = "커뮤니티 선택은 필수입니다.")
	Long communityId,

	@Schema(description = "성별", example = "남성")
	@NotNull(message = "성별 선택은 필수입니다.")
	GenderType gender,

	@Schema(description = "연령대", example = "20대")
	@NotNull(message = "연령대 선택은 필수입니다.")
	AgeRangeType ageRange
) {

	public ProfileCreateServiceRequest toServiceRequest(Long userId) {
		return ProfileCreateServiceRequest.builder()
			.personalInfo(new PersonalInfo(profileImage, new Nickname(nickname), gender, ageRange))
			.userId(new UserId(userId))
			.communityId(new CommunityId(communityId))
			.build();
	}

}