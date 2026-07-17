package com.debateseason_backend_v1.domain.profile.application.service.response;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.domain.profile.domain.AgeRangeType;
import com.debateseason_backend_v1.domain.profile.domain.CommunityType;
import com.debateseason_backend_v1.domain.profile.domain.GenderType;
import com.debateseason_backend_v1.domain.profile.domain.Nickname;
import com.debateseason_backend_v1.domain.profile.domain.Profile;
import com.debateseason_backend_v1.domain.profile.domain.ProvinceType;
import com.debateseason_backend_v1.domain.profile.domain.DistrictType;
import com.debateseason_backend_v1.domain.profile.domain.Region;
import com.debateseason_backend_v1.domain.user.domain.SocialType;
import com.fasterxml.jackson.databind.ObjectMapper;

class ProfileResponseSerializationTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	private Profile sampleProfile() {
		return Profile.create(
			1L, CommunityType.values()[0].getId(),
			Nickname.of("토론왕"), GenderType.MALE, AgeRangeType.TWENTIES,
			Region.of(ProvinceType.UNDEFINED, DistrictType.UNDEFINED),
			Region.of(ProvinceType.UNDEFINED, DistrictType.UNDEFINED)
		);
	}

	@Test
	@DisplayName("카카오 가입자의 프로필 응답은 socialType을 kakao로 직렬화한다")
	void serializesKakao() throws Exception {
		ProfileResponse response = ProfileResponse.of(
			sampleProfile(), CommunityType.values()[0], SocialType.KAKAO
		);

		String json = objectMapper.writeValueAsString(response);

		assertThat(json).contains("\"socialType\":\"kakao\"");
	}

	@Test
	@DisplayName("애플 가입자의 프로필 응답은 socialType을 apple로 직렬화한다")
	void serializesApple() throws Exception {
		ProfileResponse response = ProfileResponse.of(
			sampleProfile(), CommunityType.values()[0], SocialType.APPLE
		);

		String json = objectMapper.writeValueAsString(response);

		assertThat(json).contains("\"socialType\":\"apple\"");
	}
}
