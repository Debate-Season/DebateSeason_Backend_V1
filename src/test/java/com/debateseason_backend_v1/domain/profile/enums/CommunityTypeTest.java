package com.debateseason_backend_v1.domain.profile.enums;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;

class CommunityTypeTest {

	@Nested
	@DisplayName("ID로 커뮤니티 찾기")
	class FindById {

		@Test
		@DisplayName("유효한 ID로 커뮤니티를 찾을 수 있다")
		void findCommunityWithValidId() {
			// given
			Long validId = 1L; // DC_INSIDE

			// when
			CommunityType communityType = CommunityType.findById(validId);

			// then
			assertThat(communityType).isEqualTo(CommunityType.DC_INSIDE);
			assertThat(communityType.getName()).isEqualTo("디시인사이드");
		}

		@Test
		@DisplayName("유효하지 않은 ID로 찾으면 예외가 발생한다")
		void throwsExceptionWithInvalidId() {
			// given
			Long invalidId = 999L;

			// when & then
			assertThatThrownBy(() -> CommunityType.findById(invalidId))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.NOT_FOUND_COMMUNITY);
		}
	}

	@Nested
	@DisplayName("이름으로 커뮤니티 찾기")
	class FindByName {

		@Test
		@DisplayName("유효한 이름으로 커뮤니티를 찾을 수 있다")
		void findCommunityWithValidName() {
			// given
			String validName = "디시인사이드";

			// when
			CommunityType communityType = CommunityType.findByName(validName);

			// then
			assertThat(communityType).isEqualTo(CommunityType.DC_INSIDE);
			assertThat(communityType.getId()).isEqualTo(1L);
		}

		@Test
		@DisplayName("유효하지 않은 이름으로 찾으면 예외가 발생한다")
		void throwsExceptionWithInvalidName() {
			// given
			String invalidName = "존재하지않는커뮤니티";

			// when & then
			assertThatThrownBy(() -> CommunityType.findByName(invalidName))
				.isInstanceOf(CustomException.class)
				.hasFieldOrPropertyWithValue("codeInterface", ErrorCode.NOT_FOUND_COMMUNITY);
		}
	}

	@Nested
	@DisplayName("ID 유효성 검사")
	class IsValidId {

		@Test
		@DisplayName("유효한 ID는 true를 반환한다")
		void returnsTrueForValidId() {
			// given
			Long validId = 1L; // DC_INSIDE

			// when
			boolean isValid = CommunityType.isValidId(validId);

			// then
			assertThat(isValid).isTrue();
		}

		@Test
		@DisplayName("유효하지 않은 ID는 false를 반환한다")
		void returnsFalseForInvalidId() {
			// given
			Long invalidId = 999L;

			// when
			boolean isValid = CommunityType.isValidId(invalidId);

			// then
			assertThat(isValid).isFalse();
		}
	}

	@Test
	@DisplayName("모든 커뮤니티는 필수 속성을 가지고 있다")
	void allCommunitiesHaveRequiredProperties() {
		// when & then
		for (CommunityType communityType : CommunityType.values()) {
			assertThat(communityType.getId()).isNotNull();
			assertThat(communityType.getName()).isNotEmpty();
			assertThat(communityType.getIconUrl()).isNotEmpty();
		}
	}

	@Test
	@DisplayName("커뮤니티 ID는 모두 고유하다")
	void communityIdsAreUnique() {
		// given
		CommunityType[] communities = CommunityType.values();

		// when & then
		for (int i = 0; i < communities.length; i++) {
			for (int j = i + 1; j < communities.length; j++) {
				assertThat(communities[i].getId()).isNotEqualTo(communities[j].getId());
			}
		}
	}
}