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
public class MLBPARK {


	private final MediaRepository mediaRepository;

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


		// 시간순으로 셔플하자
		// LocalDateTime now = LocalDateTime.now().withNano(0);
		// 분전(min),시간전(hour),일전(day)

		// #bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(2) > div > a:nth-child(2) > img
		// #bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(5) > div > a:nth-child(2) > img
		WebDriver driver = new ChromeDriver(options);

		try {
			// 정치/시사 + 인기
			driver.get("https://mlbpark.donga.com/mp/best.php?b=bullpen&m=like");

			//System.out.println(driver.getPageSource());

			for (int i = 1; i <= 5; i++) {
				WebElement webElement = driver.findElement(By.cssSelector(
					"#container > div.contents > div.left_cont > div > div.tab_contents > div.tbl_box > table > tbody > tr:nth-child("
						+ i + ")"));
				String id = webElement.findElement(By.cssSelector("td:nth-child(1)")).getText();

				WebElement titleElement = webElement.findElement(By.cssSelector("td:nth-child(2) > a"));
				String title = titleElement.getText();

				String href = titleElement.getAttribute("href");

				//String date = webElement.findElement(By.cssSelector("td:nth-child(4) > span")).getText();

				LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

				Media mlbPark = Media.builder()
					.title(title)
					.url(href)
					.src(null)// 이미지 원래 없음.
					.category("사회")
					.media("엠엘비파크")
					.type("community")
					.count(0)
					.createdAt(now)
					.build();

				Thread.sleep(1000);

				mediaRepository.save(mlbPark);
			}

			// #container > div.contents > div.left_cont > div > div.tab_contents > div.tbl_box > table > tbody > tr:nth-child(1) > td:nth-child(1)
			// #container > div.contents > div.left_cont > div > div.tab_contents > div.tbl_box > table > tbody > tr:nth-child(1) > td:nth-child(2) > a
			// #container > div.contents > div.left_cont > div > div.tab_contents > div.tbl_box > table > tbody > tr:nth-child(1) > td:nth-child(3) > span
			// #container > div.contents > div.left_cont > div > div.tab_contents > div.tbl_box > table > tbody > tr:nth-child(1) > td:nth-child(4) > span

		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally {
			driver.quit();
		}



	}
}
