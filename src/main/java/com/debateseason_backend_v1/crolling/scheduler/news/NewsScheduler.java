package com.debateseason_backend_v1.crolling.scheduler.news;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.repository.MediaRepository;
import com.debateseason_backend_v1.domain.repository.entity.Media;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class NewsScheduler {

	private final MediaRepository mediaRepository;

	private final Map<String,String> newsList = new HashMap<>();

	//@Scheduled(cron = "0 0 17 * * ?",zone = "Asia/Seoul")
	public void doCroll() throws InterruptedException {


		WebDriverManager.chromedriver().setup();


		// 랜덤 User-Agent 리스트
		String[] userAgents = {
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
		};

		// 랜덤 User-Agent 선택
		Random rand = new Random();
		String userAgent = userAgents[rand.nextInt(userAgents.length)];

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new"); // headless 모드
		options.addArguments("user-agent=" + userAgent); // 랜덤 User-Agent 설정

		// 추가적인 헤더 설정 (필요시)
		options.addArguments("--disable-blink-features=AutomationControlled"); // 이거 false 나와야 자동화 회피 가능하다
		options.addArguments("--disable-gpu"); // GPU 비활성화 (성능 개선)

		// 한국경제 https://media.naver.com/press/015
		// 매일경제 https://media.naver.com/press/009
		// 한계례   https://media.naver.com/press/028
		// 조선일보 https://media.naver.com/press/023
		// 전자신문 https://media.naver.com/press/030
		// 중앙일보 https://media.naver.com/press/025
		// 연합뉴스 https://media.naver.com/press/422
		// YTN    https://media.naver.com/press/052
		// MBC    https://media.naver.com/press/214
		// SBS    https://media.naver.com/press/055
		// KBS    https://media.naver.com/press/056

		List<Integer> category = Arrays.asList(100,101,102,104);

		// #ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(1) > a.press_edit_news_link._es_pc_link > span.press_edit_news_thumb > img
		// Map<String,String> newsList = new HashMap<>();
		newsList.put("한국경제","https://media.naver.com/press/015");
		newsList.put("매일경제","https://media.naver.com/press/009");
		newsList.put("한계례","https://media.naver.com/press/028");
		newsList.put("조선일보","https://media.naver.com/press/023");
		newsList.put("전자신문","https://media.naver.com/press/030");
		newsList.put("중앙일보","https://media.naver.com/press/025");
		newsList.put("연합뉴스","https://media.naver.com/press/422");
		newsList.put("YTN","https://media.naver.com/press/052");
		newsList.put("MBC","https://media.naver.com/press/214");
		newsList.put("SBS","https://media.naver.com/press/055");
		newsList.put("KBS","https://media.naver.com/press/056");


		Set<String> keys = newsList.keySet();

		WebDriver driver = null;

		// 시간순으로 셔플하자
		//LocalDateTime now = LocalDateTime.now();//.withNano(0);

		// 분전(min),시간전(hour),일전(day)

		try{
		for(String s : keys) {

				String url = newsList.get(s);

				for (Integer i : category) {

					driver = new ChromeDriver(options);

					driver.get(url + "?sid=" + i.toString());

					for (int j = 1; j <= 2; j++) {

						LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

						// 1. Link
						WebElement aTag = driver.findElement(
							By.cssSelector("#ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(" + j
								+ ") > a.press_edit_news_link._es_pc_link"));
						String href = aTag.getAttribute("href");

						// 2. title
						WebElement titleElement = driver.findElement(By.cssSelector(
							"#ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(" + j
								+ ") > a.press_edit_news_link._es_pc_link > span.press_edit_news_text > span.press_edit_news_title"));
						String title = titleElement.getText();

						// 3. time
						WebElement timeElement = driver.findElement(By.cssSelector(
							"#ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(" + j
								+ ") > a > span.press_edit_news_text > span.r_ico_b.r_modify"));
						String outdpated = timeElement.getText();

						// 4. img
						String image = null;
						try{
							WebElement imgElement = driver.findElement(By.cssSelector(
								"#ct > div._tab_panel > div:nth-child(1) > ul > li:nth-child(" + j
									+ ") > a.press_edit_news_link._es_pc_link > span.press_edit_news_thumb > img"));
							image = imgElement.getAttribute("src");
						}
						catch (NoSuchElementException e){ // 없으면 image는 null이다.

						}


						LocalDateTime time = null;
						// 분전(min),시간전(hour),일전(day)

						if (outdpated.contains("분전")) {
							outdpated = outdpated.replace("분전", "");
							time = now.minusMinutes(Integer.parseInt(outdpated));
						} else if (outdpated.contains("시간전")) {
							outdpated = outdpated.replace("시간전", "");
							time = now.minusHours(Integer.parseInt(outdpated));

						} else if (outdpated.contains("일전")) {
							outdpated = outdpated.replace("일전", "");
							time = now.minusDays(Integer.parseInt(outdpated));

						}

						// 카테고리 넣기
						// 정치 https://media.naver.com/press/422?sid=100
						// 경제 https://media.naver.com/press/422?sid=101
						// 사회 https://media.naver.com/press/422?sid=102
						// IT https://media.naver.com/press/422?sid=105
						String categoryName = null;
						if (i == 100) {
							categoryName = "정치";
						} else if (i == 101) {
							categoryName = "경제";
						} else if (i == 102) {
							categoryName = "사회";
						} else if (i == 104) {
							categoryName = "세계";
						}

						Media news = Media.builder()
							.title(title)
							.url(href)
							.src(image)
							.category(categoryName)
							.media(s)
							.type("news")
							.count(0)
							.createdAt(time)
							.build();
						mediaRepository.save(news);

						Thread.sleep(1000);// 분까지 겹치는 경우를 방지해서 일부러 1초 기다림
					}

					driver.quit();

					Thread.sleep(15000); // 15s에 1번씩 가져옴.

				}
			}

		}
		catch (Exception e){
			throw new RuntimeException(e);
		}
		finally {
			driver.quit();
		}
	}
}
