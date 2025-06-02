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
public class TodayHumor { // 에러발생

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

		// 정치/시사 + 인기
		driver.get("https://www.todayhumor.co.kr/board/list.php?table=bestofbest");

		// body > div.whole_box > div > div > table > tbody > tr:nth-child(2)
		// body > div.whole_box > div > div > table > tbody > tr:nth-child(2) > td.subject > a

		for(int i=2; i<2+5; i++){

			WebElement webElement = driver.findElement(By.cssSelector("body > div.whole_box > div > div > table > tbody > tr:nth-child("+i+")"));

			WebElement hrefElement = webElement.findElement(By.cssSelector("td.subject > a"));
			String title = hrefElement.getText();
			String url = hrefElement.getAttribute("href");
			String dateElement = webElement.findElement(By.cssSelector("td.date")).getText();
			
			// 이미지 null 처리, null이면 null인대로 들어간다.
			String img = null;
			String[] datelist = dateElement.split(" ");// 25/05/06, 16:08

			String dateparts = datelist[0];// 25/05/06

			String []dateparts2 = dateparts.split("/");

			String year = "20"+dateparts2[0]+"-";// 2025-
			String month = dateparts2[1]+"-";// 05-
			String day = dateparts2[2]+" ";// 06

			// 2025-05-06(공백s)
			String joineddate = year+month+day;

			// 16:08
			String timeparts = datelist[1];// 16:08

			// 2025-05-06 16:08
			String date = joineddate+timeparts;

			// Create a DateTimeFormatter with the appropriate pattern
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			// Parse the string to a LocalDateTime object
			LocalDateTime dateTime = LocalDateTime.parse(date, formatter);

			Media todayHumor = Media.builder()
				.title(title)
				.url(url)
				.src(img)
				.category("사회")
				.media("오늘의유머")
				.type("community")
				.count(0)
				.createdAt(dateTime)
				.build();

			mediaRepository.save(todayHumor);

		}
	}
}
