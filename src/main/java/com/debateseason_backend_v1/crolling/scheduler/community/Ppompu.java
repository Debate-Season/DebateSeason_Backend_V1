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
public class Ppompu {

	private final MediaRepository mediaRepository;

	private WebDriver driver ;
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
		options.addArguments("--headless=new");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");

		options.addArguments("user-agent=" + userAgent); // 랜덤 User-Agent 설정

		// 추가적인 헤더 설정 (필요시)
		options.addArguments("--disable-blink-features=AutomationControlled"); // 이거 false 나와야 자동화 회피 가능하다
		options.addArguments("--disable-gpu"); // GPU 비활성화 (성능 개선)


		// 시간순으로 셔플하자
		// LocalDateTime now = LocalDateTime.now().withNano(0);
		// 분전(min),시간전(hour),일전(day)

		driver = new ChromeDriver(options);

		driver.get("https://www.ppomppu.co.kr/hot.php?category=2");

		//#revolution_main_table > tbody > tr:nth-child(11)
		//#revolution_main_table > tbody > tr:nth-child(12)
		//#revolution_main_table > tbody > tr:nth-child(13)
		//#revolution_main_table > tbody > tr:nth-child(14)
		//#revolution_main_table > tbody > tr:nth-child(15)

		try {
			extractData(4);
			extractData(5);
			extractData(6);
			extractData(7);
			extractData(8);
		}
		catch (Exception e){
			throw new RuntimeException(e);
		}
		finally {
			driver.quit();
		}
	}

	private void extractData(int boardIndex){

		// body > div.wrapper > div.contents > div.container > div > div.board_box > table > tbody > tr:nth-child(4)
		// body > div.wrapper > div.contents > div.container > div > div.board_box > table > tbody > tr:nth-child(5)


		//#revolution_main_table > tbody > tr:nth-child(11) > td:nth-child(2) > img.baseList-img
		WebElement e = driver.findElement(By.cssSelector("body > div.wrapper > div.contents > div.container > div > div.board_box > table > tbody > tr:nth-child("+boardIndex+")"));
		
		WebElement imageElement = e.findElement(By.cssSelector("td.baseList-space.title > a > img"));
		String image = imageElement.getAttribute("src") != null ? imageElement.getAttribute("src") : null;

		String url = e.findElement(By.cssSelector("td.baseList-space.title > a")).getAttribute("href");

		String title = e.findElement(By.cssSelector("td.baseList-space.title > div > div > a:nth-child(2)")).getText();

		String beforeTime = e.findElement(By.cssSelector("td:nth-child(5)")).getText();

		// time 가공
		LocalDateTime now = LocalDateTime.now().withNano(0);

		int hour = now.getHour();
		int minute = now.getMinute();
		int second = now.getSecond();

		String hourMinuteSec = new StringBuilder()
			.append(hour)
			.append(":")
			.append(minute)
			.append(":")
			.append(second)
			.toString()
			;

		String updatedTimeString = now.toString().replace(hourMinuteSec, beforeTime);

		LocalDateTime localDateTime = LocalDateTime.parse(updatedTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

		Media ppomPu = Media.builder()
			.title(title)
			.url(url)
			.src(image)
			.category("사회")
			.media("뽐뿌")
			.type("community")
			.count(0)
			.createdAt(localDateTime)
			.build();

			mediaRepository.save(ppomPu);
	}

}
