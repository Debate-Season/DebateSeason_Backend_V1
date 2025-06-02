package com.debateseason_backend_v1.crolling.scheduler.community;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

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
public class FmKorea {

	private final MediaRepository mediaRepository;
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

		// #bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(2) > div > a:nth-child(2) > img
		// #bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(5) > div > a:nth-child(2) > img
		WebDriver driver = new ChromeDriver(options);

		// 정치/시사 + 인기
		driver.get("https://www.fmkorea.com/index.php?mid=politics&sort_index=pop&order_type=desc");

		// #bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(1) > div > h3 > a  // 제목 링크
		// #bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(1) > div > div:nth-child(5) > span.regdate // 시간

		// #bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(2) > div > h3 > a
		// #bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(2) > div > div:nth-child(5) > span.regdate

		// 오늘 YYYY-MM-DD
		LocalDate today = LocalDate.now();

		try {
			for (int i = 1; i <= 5; i++) {
				WebElement titleElement = driver.findElement(By.cssSelector(
					"#bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(" + i + ") > div > h3 > a"));
				WebElement timeElement = driver.findElement(By.cssSelector(
					"#bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(" + i
						+ ") > div > div:nth-child(5) > span.regdate"));

				String image = null;

				// 이미지가 null이면 null인 상태로 넘어간다.
				try {
					WebElement imgElement = driver.findElement(By.cssSelector(
						"#bd_4180795_0 > div > div.fm_best_widget._bd_pc > ul > li:nth-child(" + i
							+ ") > div > a:nth-child(2) > img"));

					image = imgElement.getAttribute("src");
				} catch (NoSuchElementException e) {// 이미지가 없는 경우 발동이 된다. 그냥 패스

				}

				String timeString = today + " " + timeElement.getText();

				// Create a DateTimeFormatter with the appropriate pattern
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

				// Parse the string to a LocalDateTime object
				LocalDateTime dateTime = LocalDateTime.parse(timeString, formatter);

				Media fmKorea = Media.builder()
					.title(titleElement.getText())
					.url(titleElement.getAttribute("href"))
					.src(image)
					.category("정치")
					.media("에펨코리아")
					.type("community")
					.count(0)
					.createdAt(dateTime)
					.build();

				mediaRepository.save(fmKorea);
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
