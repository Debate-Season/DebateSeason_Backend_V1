package com.debateseason_backend_v1.crolling.scheduler.youtube;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.repository.MediaRepository;
import com.debateseason_backend_v1.domain.repository.entity.Media;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class YoutubeScheduler {

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
		options.addArguments("--headless=new"); // headless 모드
		options.addArguments("user-agent=" + userAgent); // 랜덤 User-Agent 설정

		// 추가적인 헤더 설정 (필요시)
		options.addArguments("--disable-blink-features=AutomationControlled"); // 이거 false 나와야 자동화 회피 가능하다
		options.addArguments("--disable-gpu"); // GPU 비활성화 (성능 개선)

		LocalDateTime now = LocalDateTime.now().withNano(0);// 현재 크롤링 시간

		WebDriver driver = new ChromeDriver(options);

		driver.get("https://www.youtube.com/feed/trending");


		// #thumbnail > yt-image > img

		// 컨테이너 통째로 가져오는 법 찾아 볼 것
		List<WebElement> elementList = driver.findElements(By.cssSelector("#grid-container"));


		//System.out.println(elementList.get(0).getText());//findElement(By.cssSelector("#thumbnail > yt-image > img")).getAttribute("src"));
		// #thumbnail > yt-image > img
		WebElement targetWebElement = elementList.get(1);

		List<WebElement> tmpList = targetWebElement.findElements(By.cssSelector("#grid-container > ytd-video-renderer"));

		int count = 0;

		try{
			// #thumbnail
			// #thumbnail > yt-image > img
			for(WebElement e: tmpList){

				if(count>=5){
					break;
				}

				String title = e.findElement(By.cssSelector("#video-title")).getText();
				String url = e.findElement(By.cssSelector("#thumbnail")).getAttribute("href");

				/*
				String src = e.findElement(By.cssSelector("#thumbnail > yt-image > img")).getAttribute("src");
				System.out.println("scr: "+src);

				 */
				WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", e);

				String src = wait.until(driver2 -> {
					try {
						WebElement img = e.findElement(By.cssSelector("#thumbnail > yt-image > img"));
						String s = img.getAttribute("src");
						return (s != null && !s.isEmpty()) ? s : null;
					} catch (Exception ex) {
						return null;
					}
				});

				String media = e.findElement(By.cssSelector("#text > a")).getText();


				Media youtube = Media.builder()
					.title(title)
					.url(url)
					.src(src)
					.category("사회")
					.media(media)
					.type("youtube")
					.count(0)
					.createdAt(now)
					.build()
					;
				mediaRepository.save(youtube);

				count++;
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
