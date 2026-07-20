package com.debateseason_backend_v1;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;

@EnableScheduling
@OpenAPIDefinition(servers = {@Server(url = "${swagger-base-url}", description = "Swagger https 프로토콜 적용")})
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class DebateSeasonBackendV1Application {

	// LocalDateTime.now()의 기준 타임존.
	// 운영 서버(Lightsail) OS가 UTC라 JVM 기본값도 UTC가 되므로 여기서 KST로 고정한다.
	// 서버/인프라 설정이 아닌 코드에 두는 이유: 서버를 재생성해도 유실되지 않게 하기 위함.
	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(DebateSeasonBackendV1Application.class, args);
	}

}
