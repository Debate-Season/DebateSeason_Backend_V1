package com.debateseason_backend_v1.crolling.scheduler.community;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.openqa.selenium.By;
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
public class RuliWeb {

	private final MediaRepository mediaRepository;

	private WebDriver driver ;
	@Scheduled(cron = "0 0 17 * * ?",zone = "Asia/Seoul")
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
		options.addArguments("--headless=new");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");

		options.addArguments("user-agent=" + userAgent); // 랜덤 User-Agent 설정

		// 추가적인 헤더 설정 (필요시)
		options.addArguments("--disable-blink-features=AutomationControlled"); // 이거 false 나와야 자동화 회피 가능하다
		options.addArguments("--disable-gpu"); // GPU 비활성화 (성능 개선)

		// 분전(min),시간전(hour),일전(day)

		driver = new ChromeDriver(options);

		driver.get("https://bbs.ruliweb.com/best/political");

		try {
			// 재사용될 부분
			extractData(1);
			extractData(2);
			extractData(3);
			extractData(4);
			extractData(5);
		}
		catch (Exception e){
			throw new RuntimeException(e);
		}
		finally {
			driver.quit();
		}

	}

	private  void extractData(int boardIndex){

			// #best_body > table > tbody > tr:nth-child(1) > td.subject > a
			WebElement element1 = driver.findElement(
				By.cssSelector("#best_body > table > tbody > tr:nth-child(" + boardIndex + ")"));

			WebElement titleElement = element1.findElement(
				By.cssSelector("tr:nth-child(" + boardIndex + ") > td.subject > a"));

			// #best_body > table > tbody > tr:nth-child(1) > td.time
			WebElement timeElement = driver.findElement(By.cssSelector("td.time"));

			// time 가공
			LocalDateTime now = LocalDateTime.now().withNano(0);

			int hour = now.getHour();
			int minute = now.getMinute();
			int second = now.getSecond();

			String beforeTime = timeElement.getText() + ":" + second;

			String hourMinuteSec = new StringBuilder()
				.append(hour)
				.append(":")
				.append(minute)
				.append(":")
				.append(second)
				.toString();

			String updatedTimeString = now.toString().replace(hourMinuteSec, beforeTime);

			LocalDateTime localDateTime = LocalDateTime.parse(updatedTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

			Media RuliWeb = Media.builder()
				.title(titleElement.getText())
				.url(titleElement.getAttribute("href"))
				.src(null)
				.category("사회")
				.media("루리웹")
				.type("community")
				.count(0)
				.createdAt(localDateTime)
				.build();

			mediaRepository.save(RuliWeb);
		}

}
