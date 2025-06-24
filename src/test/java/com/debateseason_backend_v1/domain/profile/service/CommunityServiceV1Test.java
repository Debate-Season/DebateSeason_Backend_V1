package com.debateseason_backend_v1.domain.profile.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.debateseason_backend_v1.domain.profile.application.service.CommunityServiceV1;
import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.profile.application.service.response.CommunityResponse;

class CommunityServiceV1Test {

	private final CommunityServiceV1 communityService = new CommunityServiceV1();

	@Nested
	@DisplayName("커뮤니티 목록 조회")
	class GetCommunities {

		@Test
		@DisplayName("모든 커뮤니티 목록을 조회할 수 있다")
		void getAllCommunities() {
			// when
			List<CommunityResponse> communities = communityService.getCommunities();

			// then
			assertThat(communities).isNotEmpty();
			assertThat(communities.size()).isEqualTo(CommunityType.values().length);
		}

		@Test
		@DisplayName("커뮤니티 목록은 무소속이 가장 먼저 나온다")
		void independentCommunityFirstOrder() {
			// when
			List<CommunityResponse> communities = communityService.getCommunities();

			// then
			assertThat(communities.get(0).name()).isEqualTo("무소속");
		}

		@Test
		@DisplayName("한글 커뮤니티는 영문 커뮤니티보다 먼저 정렬된다")
		void koreanBeforeEnglishOrder() {
			// when
			List<CommunityResponse> communities = communityService.getCommunities();

			// then
			boolean foundKorean = false;
			boolean foundEnglish = false;

			for (CommunityResponse community : communities) {
				if (community.name().equals("무소속")) {
					continue;
				}

				char firstChar = community.name().charAt(0);

				if (!foundEnglish && (firstChar >= 'A' && firstChar <= 'Z') || (firstChar >= 'a' && firstChar <= 'z')) {
					foundEnglish = true;
					assertThat(foundKorean).isTrue();
				}

				if (!foundKorean && firstChar >= '가' && firstChar <= '힣') {
					foundKorean = true;
				}
			}
		}
	}

	@Nested
	@DisplayName("커뮤니티 이름으로 검색")
	class SearchByName {

		@Test
		@DisplayName("이름에 포함된 문자열로 커뮤니티를 검색할 수 있다")
		void searchCommunityByPartialName() {
			// given
			String searchKeyword = "디시";

			// when
			List<CommunityResponse> results = communityService.searchByName(searchKeyword);

			// then
			assertThat(results).isNotEmpty();
			assertThat(results).allMatch(community -> community.name().contains(searchKeyword));
		}

		@Test
		@DisplayName("빈 문자열로 검색하면 모든 커뮤니티가 반환된다")
		void searchWithEmptyString() {
			// given
			String emptyKeyword = "   ";

			// when
			List<CommunityResponse> results = communityService.searchByName(emptyKeyword);

			// then
			assertThat(results.size()).isEqualTo(CommunityType.values().length);
		}

		@Test
		@DisplayName("검색 결과가 없으면 빈 리스트를 반환한다")
		void searchWithNoResults() {
			// given
			String nonExistentKeyword = "존재하지않는커뮤니티";

			// when
			List<CommunityResponse> results = communityService.searchByName(nonExistentKeyword);

			// then
			assertThat(results).isEmpty();
		}
	}
}