package com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 유튜브 Search API 응답 파싱 회귀 테스트.
 *
 * 2026-09-15, 유튜브가 응답의 snippet.thumbnails 에 standard/maxres/fhd 를 추가하면서
 * 뉴스 크롤러 5개(KBS·MBC·SBS·YTN·연합)가 동시에 죽었다. DTO 가 default/medium/high
 * 3개만 알고 있었고 ObjectMapper 의 FAIL_ON_UNKNOWN_PROPERTIES 가 기본값(true)이라
 * 모르는 필드 하나에 역직렬화 전체가 터진 것이다. 앱은 9/3부터 그대로였으니
 * 배포 없이 외부 응답 변화만으로 터졌다.
 *
 * 유튜브는 앞으로도 필드를 늘릴 수 있으므로, "모르는 필드가 와도 깨지지 않는다" 를
 * 계약으로 고정한다.
 */
class YouTubeSearchResponseParsingTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	@DisplayName("모르는 썸네일 사이즈(standard·maxres·fhd)가 와도 파싱된다")
	void 모르는_썸네일_사이즈를_무시한다() throws Exception {
		// 2026-09-15 운영에서 실제로 받은 응답의 형태.
		String json = """
			{
			  "kind": "youtube#searchListResponse",
			  "items": [{
			    "id": { "kind": "youtube#video", "videoId": "SI1DttIq7p8" },
			    "snippet": {
			      "publishedAt": "2026-09-15T14:21:04Z",
			      "channelTitle": "KBS News",
			      "title": "[속보] 테스트 제목",
			      "thumbnails": {
			        "default":  { "url": "https://i.ytimg.com/vi/x/default.jpg",  "width": 120, "height": 90 },
			        "medium":   { "url": "https://i.ytimg.com/vi/x/mqdefault.jpg", "width": 320, "height": 180 },
			        "high":     { "url": "https://i.ytimg.com/vi/x/hqdefault.jpg", "width": 480, "height": 360 },
			        "standard": { "url": "https://i.ytimg.com/vi/x/sddefault.jpg", "width": 640, "height": 480 },
			        "maxres":   { "url": "https://i.ytimg.com/vi/x/maxres.jpg",    "width": 1280, "height": 720 },
			        "fhd":      { "url": "https://i.ytimg.com/vi/x/fhd.jpg",       "width": 1920, "height": 1080 }
			      }
			    }
			  }]
			}
			""";

		YouTubeSearchResponse response = mapper.readValue(json, YouTubeSearchResponse.class);

		List<Item> items = response.getItems();
		assertThat(items).hasSize(1);

		Snippet snippet = items.get(0).getSnippet();
		assertThat(items.get(0).getId().getVideoId()).isEqualTo("SI1DttIq7p8");
		assertThat(snippet.getTitle()).isEqualTo("[속보] 테스트 제목");

		// 크롤러가 실제로 꺼내 쓰는 값. "default" 는 자바 예약어라 @JsonProperty 로 매핑돼 있다.
		Thumbnail thumbnail = snippet.getThumbnails().getDefaultThumbnail();
		assertThat(thumbnail).isNotNull();
		assertThat(thumbnail.getUrl()).isEqualTo("https://i.ytimg.com/vi/x/default.jpg");
	}

	@Test
	@DisplayName("응답 어느 깊이에 모르는 필드가 추가돼도 파싱된다")
	void 모든_계층에서_모르는_필드를_무시한다() throws Exception {
		// 유튜브가 필드를 늘리는 자리는 thumbnails 만이 아니다.
		// 최상위·item·id·snippet·thumbnail 각 계층에 미지의 필드를 하나씩 넣어본다.
		String json = """
			{
			  "kind": "youtube#searchListResponse",
			  "newTopLevelField": { "whatever": true },
			  "items": [{
			    "kind": "youtube#searchResult",
			    "newItemField": 1,
			    "id": { "kind": "youtube#video", "videoId": "abc123", "newIdField": "x" },
			    "snippet": {
			      "publishedAt": "2026-09-15T14:21:04Z",
			      "channelTitle": "YTN",
			      "title": "제목",
			      "newSnippetField": ["a", "b"],
			      "thumbnails": {
			        "default": { "url": "https://i.ytimg.com/vi/y/default.jpg", "width": 120, "height": 90, "newThumbnailField": "z" }
			      }
			    }
			  }]
			}
			""";

		YouTubeSearchResponse response = mapper.readValue(json, YouTubeSearchResponse.class);

		Snippet snippet = response.getItems().get(0).getSnippet();
		assertThat(response.getItems().get(0).getId().getVideoId()).isEqualTo("abc123");
		assertThat(snippet.getChannelTitle()).isEqualTo("YTN");
		assertThat(snippet.getThumbnails().getDefaultThumbnail().getUrl())
			.isEqualTo("https://i.ytimg.com/vi/y/default.jpg");
	}
}
