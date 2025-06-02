package com.debateseason_backend_v1.crolling.scheduler.community;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
public class BoabaeDream {

	private final MediaRepository mediaRepository;

	@Scheduled(cron = "0 0 17 * * ?",zone = "Asia/Seoul")
	@Scheduled(fixedDelay = 1000)
	public void doCroll(){

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

		WebDriver driver = new ChromeDriver(options);

		driver.get("https://www.bobaedream.co.kr/list?code=best");

		// #boardlist > tbody > tr:nth-child(1)

		// title, a : #boardlist > tbody > tr:nth-child(1) > td.pl14 > a.bsubject

		// time : #boardlist > tbody > tr:nth-child(1) > td.date

		for(int i=1; i<=5; i++){

			WebElement webElement = driver.findElement(By.cssSelector("#boardlist > tbody > tr:nth-child(+"+i+")"));

			WebElement titleElement = webElement.findElement(By.cssSelector("td.pl14 > a.bsubject"));

			String title = titleElement.getText();
			String href = titleElement.getAttribute("href");

			String time = webElement.findElement(By.cssSelector("td.date")).getText();

			LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

			// 문자열에서 시와 분 파싱
			int hour = Integer.parseInt(time.split(":")[0]);
			int minute = Integer.parseInt(time.split(":")[1]);

			// 시:분만 21:42로 덮어쓰기, 초와 나노초는 유지
			LocalDateTime replaced = now.withHour(hour).withMinute(minute);

			Media boBaeDream = Media.builder()
				.title(title)
				.url(href)
				.src(null)
				.category("사회")
				.media("보배드림")
				.type("community")
				.count(0)
				.createdAt(replaced)
				.build();

			mediaRepository.save(boBaeDream);

		}

	}

}
