package com.debateseason_backend_v1.crolling.scheduler.community;

import java.time.LocalDate;
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
public class Clien {

	private final MediaRepository mediaRepository;

	@Scheduled(cron = "0 0 17 * * ?",zone = "Asia/Seoul")
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

		try {
			// 정치/시사 + 인기
			driver.get("https://www.clien.net/service/group/clien_all?&od=T33");

			for (int i = 1; i < 6; i++) {
				// body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child(1)
				WebElement webElement =
					driver.findElement(By.cssSelector(
						"body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child("
							+ i + ")"));

				// body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child(1) > div.list_title
				WebElement hrefElement = webElement.findElement(By.cssSelector("div.list_title"));

				// body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child(1) > div.list_title > a.list_subject
				String url = hrefElement.findElement(By.cssSelector("a.list_subject")).getAttribute("href");
				// body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child(1) > div.list_title > a.list_subject > span.subject_fixed
				String title = hrefElement.findElement(By.cssSelector("a.list_subject > span.subject_fixed")).getText();

				// body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child(1) > div.list_time > span > span
				String time = webElement.findElement(By.cssSelector("div.list_time > span")).getText();

				LocalDate today = LocalDate.now();

				String date = today + " " + time;

				// Create a DateTimeFormatter with the appropriate pattern
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				// Parse the string to a LocalDateTime object
				LocalDateTime dateTime = LocalDateTime.parse(date, formatter);

				Media Clien = Media.builder()
					.title(title)
					.url(url)
					.src(null)// 이미지
					.category("사회")
					.media("클리앙")
					.type("community")
					.count(0)
					.createdAt(dateTime)
					.build();

				mediaRepository.save(Clien);
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally {
			driver.quit();;
		}


		// body > div.nav_container > div.nav_body > div.nav_content > div.content_list > div.list_content > div:nth-child(2)

	}
}
