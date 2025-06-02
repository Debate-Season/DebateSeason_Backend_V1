package com.debateseason_backend_v1.crolling.scheduler.community;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
public class HumorUniv {

	private final MediaRepository mediaRepository;
	private final int start = 3;

	//@Scheduled(cron = "0 0 17 * * ?",zone = "Asia/Seoul")
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

		driver.get("https://m.humoruniv.com/board/list.html?table=pds&st=better");

		try {
			for (int i = 0; i < 5; i++) {
				WebElement webElement = driver.findElement(
					By.cssSelector("#list_body > ul > a:nth-child(" + (start + i * 2) + ")"));

				//
				WebElement idElement = webElement.findElement(By.cssSelector("li"));
				String id = idElement.getAttribute("id");

				String numberOnly = id.replaceAll("[^0-9]", ""); // 숫자가 아닌 문자를 모두 제거

				String image = driver.findElement(
						By.cssSelector("#" + id + "> table > tbody > tr > td:nth-child(1) > div > img"))
					.getAttribute("src");
				String title = driver.findElement(By.cssSelector("#title_chk_pds-" + numberOnly)).getText();
				//#title_chk_pds-1366802

				String time = driver.findElement(
					By.cssSelector("#" + id + "> table > tbody > tr > td:nth-child(2) > div > span.extra")).getText();

				// href
				String href = driver.findElement(
					By.cssSelector("#list_body > ul > a:nth-child(" + (start + i * 2) + ")")).getAttribute("href");

				// 2. 시간 부분만 추출
				String timePart = time.split(" ")[1]; // "07:31"

				// 3. 시:분 파싱
				DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
				LocalTime parsedTime = LocalTime.parse(timePart, timeFormatter);

				// 4. 현재 날짜에 시:분만 교체
				LocalDateTime now = LocalDateTime.now();
				LocalDateTime updatedDateTime = now
					.withHour(parsedTime.getHour())
					.withMinute(parsedTime.getMinute())
					.withSecond(0)
					.withNano(0);

				Media humorUniv = Media.builder()
					.title(title)
					.url(href)
					.src(image)
					.category("사회")
					.media("웃긴대학")
					.type("community")
					.count(0)
					.createdAt(updatedDateTime)
					.build();

				mediaRepository.save(humorUniv);
			}
		}
		catch (NullPointerException e){
			e.printStackTrace();
		}
		finally {
			driver.quit();
		}


	}
}
