package com.debateseason_backend_v1.domain.youtubeLive.scheduler.news;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.debateseason_backend_v1.config.YouTubeConfig;
import com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper.Id;
import com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper.Item;
import com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper.Snippet;
import com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper.Thumbnail;
import com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper.Thumbnails;
import com.debateseason_backend_v1.domain.youtubeLive.scheduler.mapper.YouTubeSearchResponse;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLive;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;
import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.entity.YoutubeLiveEntity;
import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.repository.YoutubeLiveRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class YtnNews {

	private final YoutubeLiveRepository youtubeLiveRepository;

	private final YouTubeConfig youTubeConfig;


	@Scheduled(fixedRate = 4320000) // 1.2시간 = 1시간 12분 = 4320000밀리초
	@Transactional
	public void doCroll() throws JsonProcessingException {
		activate();
	}

	private void activate() {
		// dirty-checking or save

		String key = youTubeConfig.getKey();

		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("part", "snippet");
			params.add("regionCode", "kr");
			params.add("type", "video");
			params.add("eventType", "live");
			params.add("order", "date");
			params.add("relevanceLanguage", "ko");
			params.add("videoEmbeddable", "true");
			//params.add("videoCategoryId", "25"); news는 25번
			params.add("channelId","UChlgI3UHCOnwUGzWzbJ3H5w");
			params.add("key", key); // 보통 실제 서비스에서는 보안상 환경변수로 관리합니다.

			WebClient webClient =
				WebClient.builder()
					.baseUrl("https://www.googleapis.com")
					.build();

			String result = webClient
				.get()
				.uri(
					uriBuilder ->
						uriBuilder
							.path("/youtube/v3/search")
							.queryParam("maxResults", 1)
							.queryParam("videoCategoryId", 25)
							.queryParams(params)
							.build()
				)
				.retrieve()
				.bodyToMono(String.class)
				.block();

			ObjectMapper mapper = new ObjectMapper();
			YouTubeSearchResponse response = mapper.readValue(result, YouTubeSearchResponse.class);

			// response -> items -> id -> videoId
			List<Item> items = response.getItems();

			Item item = items.get(0);
			Id id = item.getId();

			// fields

			// videoId
			String videoId = id.getVideoId(); // 1

			Snippet snippet = item.getSnippet();

			// 수정해야함!!!!!
			String createdAt = snippet.getPublishedAt(); // 2

			// 'Z' 제거
			if (createdAt.endsWith("Z")) {
				createdAt = createdAt.substring(0, createdAt.length() - 1);
			}

			// 파싱
			LocalDateTime localDateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

			String title = snippet.getTitle(); // 3
			//String content = snippet.getDescription(); // 4
			String supplier = snippet.getChannelTitle();

			// 썸네일 이미지 가져오기
			Thumbnails thumbnails = snippet.getThumbnails();

			// 썸네일에서 이미지 주소 추출
			Thumbnail thumbnail = thumbnails.getDefaultThumbnail();
			String url = thumbnail.getUrl();

			String category = "ytn"; // 5

			// 새로운 youtubelive 데이터 생성하기
			YoutubeLive newYoutubeLive = YoutubeLive.builder()
				.videoId(videoId)
				.title(title)
				.supplier(supplier)
				.category(category)
				.createAt(localDateTime)
				.src(url)
				.build();

			// 1. 가져오기
			YoutubeLiveEntity fetchedYoutubeLiveEntity = youtubeLiveRepository.fetch(category);
			YoutubeLiveDto youtubeLiveDto = newYoutubeLive.createDto();

			// 만약 null이면 -> 없음 -> 새로 넣어주자. 아니면 더티 체킹
			if (fetchedYoutubeLiveEntity == null) {

				youtubeLiveRepository.save(youtubeLiveDto);
			} else { // Dirty-Checking
				update(fetchedYoutubeLiveEntity, youtubeLiveDto);
			}

		}
		catch (WebClientResponseException e){
			if(key.equals("dummy")){
				log.error("key: "+key+"이므로, 설정값 다시 확인!");
			}
			else{
				log.error("유튜브 API 할당량 모두 소진 -> YtnNews.NewsLive");
			}
		}
		catch (JsonProcessingException e) {
			log.error("YtnNews에서 발생한 JsonProcessingException 에러");
			throw new RuntimeException(e);
		}
		catch (IndexOutOfBoundsException e){
			log.warn("YtnNews Live 진행 안함.");
		}
	}

	private void update(YoutubeLiveEntity oldData, YoutubeLiveDto newData){
		oldData.setTitle(newData.getTitle());
		oldData.setSupplier(newData.getSupplier());
		oldData.setVideoId(newData.getVideoId());
		oldData.setCreatedAt(newData.getCreateAt());
		oldData.setScr(newData.getSrc());
	}

}
